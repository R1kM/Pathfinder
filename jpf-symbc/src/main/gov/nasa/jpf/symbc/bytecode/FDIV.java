/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * Symbolic Pathfinder (jpf-symbc) is licensed under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0. 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package gov.nasa.jpf.symbc.bytecode;

import gov.nasa.jpf.symbc.numeric.Comparator;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.symbc.numeric.RealExpression;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Types;

public class FDIV extends gov.nasa.jpf.jvm.bytecode.FDIV  {

	@Override
	public Instruction execute (ThreadInfo th) {

		StackFrame sf = th.getModifiableTopFrame();
		//sf.printStackContent();

		RealExpression sym_v1 = (RealExpression) sf.getOperandAttr(0);
		float v1;
		RealExpression sym_v2 = (RealExpression) sf.getOperandAttr(1);
		float v2;

		if(sym_v1==null && sym_v2==null) {
			v1 = Types.intToFloat(sf.pop());
			v2 = Types.intToFloat(sf.pop());
			if(v1==0)
				return th.createAndThrowException("java.lang.ArithmeticException","div by 0");
			float r = v2 / v1;
			sf.push(Types.floatToInt(r), false);
			return getNext(th);
		}

		// result is symbolic expression

		if(sym_v1==null && sym_v2!=null) {
			v1 = Types.intToFloat(sf.pop());
			v2 = Types.intToFloat(sf.pop());
			if(v1==0)
				return th.createAndThrowException("java.lang.ArithmeticException","div by 0");
			sf.push(0, false);
			RealExpression result = sym_v2._div(v1);
			sf.setOperandAttr(result);
			return getNext(th);
		}

		// div by zero check affects path condition
		// sym_v1 is non-null and should be checked against zero

		ChoiceGenerator<?> cg;
		boolean condition;

		if (!th.isFirstStepInsn()) { // first time around
			cg = new PCChoiceGenerator(2);
			((PCChoiceGenerator)cg).setOffset(this.position);
			((PCChoiceGenerator)cg).setMethodName(this.getMethodInfo().getFullName());
			th.getVM().getSystemState().setNextChoiceGenerator(cg);
			return this;
		} else {  // this is what really returns results
			cg = th.getVM().getSystemState().getChoiceGenerator();
			assert (cg instanceof PCChoiceGenerator) : "expected PCChoiceGenerator, got: " + cg;
			condition = (Integer)cg.getNextChoice()==0 ? false: true;
		}


		v1 = Types.intToFloat(sf.pop());
		v2 = Types.intToFloat(sf.pop());
		sf.push(0, false);



		PathCondition pc;
		ChoiceGenerator<?> prev_cg = cg.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);

		if (prev_cg == null)
			pc = new PathCondition();
		else
			pc = ((PCChoiceGenerator)prev_cg).getCurrentPC();

		assert pc != null;

		if(condition) { // check div by zero
			pc._addDet(Comparator.EQ, sym_v1, 0);
			if(pc.simplify())  { // satisfiable
				((PCChoiceGenerator) cg).setCurrentPC(pc);

				return th.createAndThrowException("java.lang.ArithmeticException","div by 0");
			}
			else {
				th.getVM().getSystemState().setIgnored(true);
				return getNext(th);
			}
		}
		else {
			pc._addDet(Comparator.NE, sym_v1, 0);
			if(pc.simplify())  { // satisfiable
				((PCChoiceGenerator) cg).setCurrentPC(pc);

				// set the result
				RealExpression result;
				if(sym_v2!=null)
					result = sym_v2._div(sym_v1);
				else
					result = sym_v1._div_reverse(v2);

				sf = th.getModifiableTopFrame();
				sf.setOperandAttr(result);
				return getNext(th);

			}
			else {
				th.getVM().getSystemState().setIgnored(true);
				return getNext(th);
			}
		}


	}

}

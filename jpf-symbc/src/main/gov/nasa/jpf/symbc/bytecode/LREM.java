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


import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.expressions.Constant;
import gov.nasa.jpf.constraints.expressions.NumericBooleanExpression;
import gov.nasa.jpf.constraints.expressions.NumericComparator;
import gov.nasa.jpf.constraints.expressions.NumericCompound;
import gov.nasa.jpf.constraints.expressions.NumericOperator;
import gov.nasa.jpf.constraints.types.BuiltinTypes;
import gov.nasa.jpf.symbc.jconstraints.*;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;


/**
 * Remainder long
 * ..., value1, value2 => ..., result
 */
public class LREM extends gov.nasa.jpf.jvm.bytecode.LREM {

  @Override
  public Instruction execute (ThreadInfo th) {
	    StackFrame sf = th.getModifiableTopFrame();
		Expression<?> sym_v1_ex = (Expression<?>) sf.getOperandAttr(1);
		Expression<?> sym_v2_ex = (Expression<?>) sf.getOperandAttr(3);
		long v1, v2;

		if(sym_v1_ex==null && sym_v2_ex==null)
			return super.execute(th);// we'll still do the concrete execution

		// result is symbolic

		if(sym_v1_ex==null && sym_v2_ex!=null) {
			v1 = sf.popLong();
			v2 = sf.popLong();
			if(v1==0)
				return th.createAndThrowException("java.lang.ArithmeticException","div by 0");
			sf.pushLong(0);
            Expression<Long> sym_v1 = Translate.translateLong(sym_v1_ex, v1);
            Expression<Long> sym_v2 = Translate.translateLong(sym_v2_ex, v2);
			NumericCompound<Long> result = new NumericCompound<Long>(sym_v2, NumericOperator.DIV, sym_v1);
			sf.setLongOperandAttr(result);
			return getNext(th);
		}

		// div by zero check affects path condition
		// sym_v1 is non-null and should be checked against zero

		ChoiceGenerator<?> cg;
		boolean condition;

		if (!th.isFirstStepInsn()) { // first time around
			cg = new JPCChoiceGenerator(2);
			((JPCChoiceGenerator)cg).setOffset(this.position);
			((JPCChoiceGenerator)cg).setMethodName(this.getMethodInfo().getFullName());
			th.getVM().getSystemState().setNextChoiceGenerator(cg);
			return this;
		} else {  // this is what really returns results
			cg = th.getVM().getSystemState().getChoiceGenerator();
			assert (cg instanceof JPCChoiceGenerator) : "expected JPCChoiceGenerator, got: " + cg;
			condition = (Integer)cg.getNextChoice()==0 ? false: true;
		}


		v1 = sf.popLong();
		v2 = sf.popLong();
		sf.pushLong(0);

        Constant<Long> zero = Constant.create(BuiltinTypes.SINT64, (long)0);
        Expression<Long> sym_v1 = Translate.translateLong(sym_v1_ex, v1);
        Expression<Long> sym_v2 = Translate.translateLong(sym_v2_ex, v2);
        
		JPathCondition pc;
		ChoiceGenerator<?> prev_cg = cg.getPreviousChoiceGenerator();

		while (!((prev_cg == null) || (prev_cg instanceof JPCChoiceGenerator))) {
			prev_cg = prev_cg.getPreviousChoiceGenerator();
		}
		if (prev_cg == null)
			pc = new JPathCondition();
		else
			pc = ((JPCChoiceGenerator)prev_cg).getCurrentPC();

		assert pc != null;

		if(condition) { // check div by zero
			pc._addDet(NumericBooleanExpression.create(sym_v1, NumericComparator.EQ, zero));
			if(pc.simplify())  { // satisfiable
				((JPCChoiceGenerator) cg).setCurrentPC(pc);

				return th.createAndThrowException("java.lang.ArithmeticException","div by 0");
			}
			else {
				th.getVM().getSystemState().setIgnored(true);
				return getNext(th);
			}
		}
		else {
			pc._addDet(NumericBooleanExpression.create(sym_v1, NumericComparator.NE, zero));
			if(pc.simplify())  { // satisfiable
				((JPCChoiceGenerator) cg).setCurrentPC(pc);

				// set the result
				NumericCompound<Long> result = new NumericCompound<Long>(sym_v2, NumericOperator.REM, sym_v1);

				sf = th.getModifiableTopFrame();
				sf.setLongOperandAttr(result);
				return getNext(th);

			}
			else {
				th.getVM().getSystemState().setIgnored(true);
				return getNext(th);
			}
        }
  }

}

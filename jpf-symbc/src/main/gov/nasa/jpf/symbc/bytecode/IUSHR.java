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
import gov.nasa.jpf.constraints.expressions.BitvectorExpression;
import gov.nasa.jpf.constraints.expressions.BitvectorOperator;
import gov.nasa.jpf.symbc.jconstraints.Translate;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

public class IUSHR extends gov.nasa.jpf.jvm.bytecode.IUSHR {

	@Override
	public Instruction execute (ThreadInfo th) {
		StackFrame sf = th.getModifiableTopFrame();
		Expression<?> sym_v1_ex = (Expression<?>) sf.getOperandAttr(0); 
		Expression<?> sym_v2_ex = (Expression<?>) sf.getOperandAttr(1);
		
		if(sym_v1_ex==null && sym_v2_ex==null)
			return super.execute(th); // we'll still do the concrete execution
		else {
			int v1 = sf.pop();
			int v2 = sf.pop();
			sf.push(0, false); // for symbolic expressions, the concrete value does not matter
		
            Expression<Integer> sym_v1 = Translate.translateInt(sym_v1_ex, v1);
            Expression<Integer> sym_v2 = Translate.translateInt(sym_v2_ex, v2);

            BitvectorExpression<Integer> result = BitvectorExpression.create(sym_v2, BitvectorOperator.SHIFTUR, sym_v1);

			sf.setOperandAttr(result);
				
			return getNext(th);
		}
	}
}

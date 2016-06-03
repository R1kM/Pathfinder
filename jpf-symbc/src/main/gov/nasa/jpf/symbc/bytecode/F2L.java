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
import gov.nasa.jpf.constraints.expressions.CastExpression;
import gov.nasa.jpf.constraints.types.BuiltinTypes;

import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;


/**
 * Convert float to long
 * ..., value => ..., result
 */
public class F2L extends gov.nasa.jpf.jvm.bytecode.F2L {
	
	
	public Instruction execute (ThreadInfo th) {
		StackFrame sf = th.getModifiableTopFrame();
		Expression<?> sym_val = (Expression<?>) sf.getOperandAttr(); 
		
		if(sym_val == null) {
			  return super.execute(th); 
		  }
		else {
            Expression<Float> sym_f = sym_val.requireAs(BuiltinTypes.FLOAT);
            CastExpression<Float, Long> cast = CastExpression.create(sym_f, BuiltinTypes.SINT64);

			sf.pop();
			sf.pushLong(0); // for symbolic expressions, the concrete value does not matter
			sf.setLongOperandAttr(cast);

			return getNext(th);
		  }
	  }
}

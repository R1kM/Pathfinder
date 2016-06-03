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
 * Convert long to double
 * ..., value => ..., result
 */
public class L2D extends gov.nasa.jpf.jvm.bytecode.L2D {
	@Override
  public Instruction execute (ThreadInfo th) {
	  StackFrame sf = th.getModifiableTopFrame();
	  Expression<?> sym_lval = (Expression<?>) sf.getLongOperandAttr();
	  if(sym_lval == null) {
		  return super.execute(th); 
	  }
	  else {
        Expression<Long> sym_l = sym_lval.requireAs(BuiltinTypes.SINT64);
        CastExpression<Long, Double> cast = CastExpression.create(sym_l, BuiltinTypes.DOUBLE);

	    sf.popLong();
	    sf.pushLong(0); // for symbolic expressions, the concrete value does not matter
	    sf.setLongOperandAttr(cast);

	    return getNext(th);
	  }
  }
}

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
import gov.nasa.jpf.constraints.expressions.NumericCompound;
import gov.nasa.jpf.constraints.expressions.NumericOperator;
import gov.nasa.jpf.symbc.jconstraints.Translate;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;


/**
 * Subtract long
 * ..., value1, value2 => ..., result
 */
public class LSUB extends gov.nasa.jpf.jvm.bytecode.LSUB {

  @Override
  public Instruction execute (ThreadInfo th) {
	StackFrame sf = th.getModifiableTopFrame();
	Expression<?> sym_v1_ex = (Expression<?>) sf.getOperandAttr(1);
	Expression<?> sym_v2_ex = (Expression<?>) sf.getOperandAttr(3);
    
    if(sym_v1_ex==null && sym_v2_ex==null)
        return super.execute(th);// we'll still do the concrete execution
    else {
    	long v1 = sf.popLong();
    	long v2 = sf.popLong();
    	sf.pushLong(0); // for symbolic expressions, the concrete value does not matter

        Expression<Long> sym_v1 = Translate.translateLong(sym_v1_ex, v1);
        Expression<Long> sym_v2 = Translate.translateLong(sym_v2_ex, v2);

    	NumericCompound<Long> result = new NumericCompound<Long>(sym_v2, NumericOperator.MINUS, sym_v1);

    	sf.setLongOperandAttr(result);
	  
	    return getNext(th);
	    }
  }
}

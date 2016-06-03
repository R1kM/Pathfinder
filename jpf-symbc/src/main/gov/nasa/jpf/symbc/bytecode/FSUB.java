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
import gov.nasa.jpf.vm.Types;


/**
 * Subtract float
 * ..., value1, value2 => ..., result
 */
public class FSUB extends gov.nasa.jpf.jvm.bytecode.FSUB {

  @Override
  public Instruction execute (ThreadInfo th) {
	  
	StackFrame sf = th.getModifiableTopFrame();

    if (sf.getOperandAttr(0) == null && sf.getOperandAttr(1) == null) {
        return super.execute(th);
    }

	Expression<?> sym_v1_ex = (Expression<?>) sf.getOperandAttr(); 
	float v1 = sf.popFloat();
    Expression<Float> sym_v1 = Translate.translateFloat(sym_v1_ex, v1);
		
	Expression<?> sym_v2_ex = (Expression<?>) sf.getOperandAttr();
	float v2 = Types.intToFloat(sf.pop());
    Expression<Float> sym_v2 = Translate.translateFloat(sym_v2_ex, v2);
	
    sf.push(0, false); 
    
    NumericCompound<Float> result = new NumericCompound<Float>(sym_v1, NumericOperator.MINUS, sym_v2);
	
	sf.setOperandAttr(result);

    return getNext(th);
  }

}

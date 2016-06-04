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
import gov.nasa.jpf.constraints.expressions.UnaryMinus;
import gov.nasa.jpf.symbc.jconstraints.Translate;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;


/**
 * Negate long
 * ..., value => ..., result
 */
public class LNEG extends gov.nasa.jpf.jvm.bytecode.LNEG {

  @Override
  public Instruction execute (ThreadInfo th) {
    StackFrame sf = th.getModifiableTopFrame();

    Expression<?> sym_v1_ex = (Expression<?>) sf.getLongOperandAttr();
    
    if(sym_v1_ex==null) {
        return super.execute(th);
    } else {
        long v1 = sf.popLong();
        sf.pushLong(0);
        
        Expression<Long> symb = Translate.translateLong(sym_v1_ex, v1);

        UnaryMinus<Long> result = new UnaryMinus<Long>(symb);
        
        sf.setLongOperandAttr(result);
        
        return getNext(th);
    }
  }

}

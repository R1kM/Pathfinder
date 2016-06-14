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
import gov.nasa.jpf.constraints.expressions.ArrayExpression;
import gov.nasa.jpf.symbc.jconstraints.Translate;

import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

public class ARRAYLENGTH extends gov.nasa.jpf.jvm.bytecode.ARRAYLENGTH {

    public Object peekArrayAttr(ThreadInfo ti) {
        return ti.getTopFrame().getOperandAttr(0);
    }

    @Override
    public Instruction execute (ThreadInfo th) {
        StackFrame frame = th.getModifiableTopFrame();
        
        if (peekArrayAttr(th) == null || !(peekArrayAttr(th) instanceof ArrayExpression<?>)) {
            return super.execute(th);
        }

       ArrayExpression<?> arrayAttr = (ArrayExpression<?>)peekArrayAttr(th);
       frame.pop(1); // We pop the array
       frame.push(0, false); // The concrete value does not matter
       Expression<Integer> result = Translate.translateInt(arrayAttr.length);
       frame.setOperandAttr(result);
        
       return getNext(th);
    }
}
       
        



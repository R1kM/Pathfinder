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

//Copyright (C) 2007 United States Government as represented by the
//Administrator of the National Aeronautics and Space Administration
//(NASA).  All Rights Reserved.

//This software is distributed under the NASA Open Source Agreement
//(NOSA), version 1.3.  The NOSA has been approved by the Open Source
//Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
//directory tree for the complete NOSA document.

//THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
//KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
//LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
//SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
//A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
//THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
//DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
package gov.nasa.jpf.symbc.bytecode;


import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.expressions.BitvectorExpression;
import gov.nasa.jpf.constraints.expressions.BitvectorOperator;
import gov.nasa.jpf.constraints.expressions.CastExpression;
import gov.nasa.jpf.constraints.expressions.Constant;
import gov.nasa.jpf.constraints.types.BuiltinTypes;
import gov.nasa.jpf.symbc.numeric.*;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

public class IAND extends gov.nasa.jpf.jvm.bytecode.IAND {

	@Override
	public Instruction execute (ThreadInfo th) {
		StackFrame sf = th.getModifiableTopFrame();
		Expression<?> sym_right = sf.getOperandAttr(0, Expression.class); 
		Expression<?> sym_left = sf.getOperandAttr(1, Expression.class);
		
		if(sym_left==null && sym_right==null)
			return super.execute(th); // we'll still do the concrete execution
		else {
			int vright = sf.pop();
			int vleft = sf.pop();
			sf.push(0, false); // for symbolic expressions, the concrete value does not matter
		
            Expression<Integer> isym_left;
            Expression<Integer> isym_right;

            if (sym_left == null) {
                isym_left = Constant.create(BuiltinTypes.SINT32, vleft);
            } else if (sym_left.getType().equals(BuiltinTypes.SINT32)) {
                isym_left = sym_left.requireAs(BuiltinTypes.SINT32);
            } else {
                isym_left = CastExpression.create(sym_left, BuiltinTypes.SINT32);
            }

            if (sym_right == null) {
                isym_right = Constant.create(BuiltinTypes.SINT32, vright);
            } else if (sym_right.getType().equals(BuiltinTypes.SINT32)) {
                isym_right = sym_right.requireAs(BuiltinTypes.SINT32);
            } else {
                isym_right = CastExpression.create(sym_right, BuiltinTypes.SINT32);
            }
            
            BitvectorExpression<Integer> result = BitvectorExpression.create(isym_left, BitvectorOperator.AND, isym_right);
			sf.setOperandAttr(result);
		
			//System.out.println("Execute IADD: "+result);
		
			return getNext(th);
		}
	}	
}

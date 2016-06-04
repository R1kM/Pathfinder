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

//
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
import gov.nasa.jpf.constraints.expressions.NumericCompound;
import gov.nasa.jpf.constraints.expressions.NumericOperator;
import gov.nasa.jpf.symbc.jconstraints.Translate;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

public class ISUB extends gov.nasa.jpf.jvm.bytecode.ISUB {

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

			NumericCompound<Integer> result = new NumericCompound<Integer>(sym_v2, NumericOperator.MINUS, sym_v1);
			sf.setOperandAttr(result);
		
		
			return getNext(th);
		}
	}

}

/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * The Java Pathfinder core (jpf-core) platform is licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except
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


// author corina pasareanu corina.pasareanu@sv.cmu.edu

package gov.nasa.jpf.symbc.bytecode;

import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.api.Variable;
import gov.nasa.jpf.constraints.expressions.Constant;
import gov.nasa.jpf.constraints.expressions.ArrayExpression;
import gov.nasa.jpf.constraints.expressions.NumericBooleanExpression;
import gov.nasa.jpf.constraints.expressions.NumericComparator;
import gov.nasa.jpf.constraints.expressions.SelectExpression;
import gov.nasa.jpf.constraints.types.BuiltinTypes;

import gov.nasa.jpf.symbc.SymbolicInstructionFactory;
import gov.nasa.jpf.symbc.jconstraints.*;
import gov.nasa.jpf.vm.ArrayIndexOutOfBoundsExecutiveException;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;


/**
 * Load byte or boolean from array
 * ..., arrayref, index => ..., value
 */
public class BALOAD extends gov.nasa.jpf.jvm.bytecode.BALOAD {

	 @Override
	  public Instruction execute (ThreadInfo ti) {

          if (peekArrayAttr(ti)==null || !(peekArrayAttr(ti) instanceof ArrayExpression<?>)) {
              // In this case, the array isn't symbolic
              if (peekIndexAttr(ti) == null || !(peekIndexAttr(ti) instanceof Expression<?>)) { 
                  // In this case, the index isn't symbolic either
                  return super.execute(ti);
              }
          }

          ArrayExpression<Byte> arrayAttr = null;
          ChoiceGenerator<?> cg;
          boolean condition;
          StackFrame frame = ti.getModifiableTopFrame();
          arrayRef = frame.peek(1); // ..., arrayRef, idx

          if (!ti.isFirstStepInsn()) { // first time around
              cg = new JPCChoiceGenerator(3);
              ((JPCChoiceGenerator)cg).setOffset(this.position);
              ((JPCChoiceGenerator)cg).setMethodName(this.getMethodInfo().getFullName());
              ti.getVM().setNextChoiceGenerator(cg);
              return this;
          } else { // this is what really returns results
            cg = ti.getVM().getChoiceGenerator();
            assert (cg instanceof JPCChoiceGenerator) : "expected JPCChoiceGenerator, got: " + cg;
          }

          JPathCondition pc;
          ChoiceGenerator<?> prev_cg = cg.getPreviousChoiceGeneratorOfType(JPCChoiceGenerator.class);

          if (prev_cg == null)
              pc = new JPathCondition();
          else
              pc = ((JPCChoiceGenerator)prev_cg).getCurrentPC();

          assert pc != null;

          if (peekArrayAttr(ti)==null || !(peekArrayAttr(ti) instanceof ArrayExpression<?>)) {
              // In this case, the array isn't symbolic
              if (peekIndexAttr(ti) == null || !(peekIndexAttr(ti) instanceof Expression<?>)) { 
                  // In this case, the index isn't symbolic either
                  return super.execute(ti);
              }
              // We have a concrete array, but a symbolic index. We add all the constraints about the elements of the array, and perform the select
              ElementInfo arrayInfo = ti.getElementInfo(arrayRef);
              arrayAttr = ArrayExpression.create(BuiltinTypes.SINT8, arrayInfo.arrayLength());
              for (int i = 0; i < arrayInfo.arrayLength(); i++) {
                byte arrValue = arrayInfo.getByteElement(i);
                pc._addDet(new SelectExpression(arrayAttr, Constant.create(BuiltinTypes.SINT32, i), Constant.create(BuiltinTypes.SINT8, arrValue)));
              }
          }

          else {
              arrayAttr = (ArrayExpression<Byte>)peekArrayAttr(ti); 
             }
          Expression<Integer> indexAttr = null;
          SelectExpression se = null;

		  if (peekIndexAttr(ti)==null || !(peekIndexAttr(ti) instanceof Expression<?>)) {
              // In this case, the index isn't symbolic.
              index = frame.peek();
              indexAttr = Constant.create(BuiltinTypes.SINT32, index);

          } else {          
              indexAttr = Translate.translateInt((Expression<?>)peekIndexAttr(ti));
          }

          Variable<Byte> val = Variable.create(BuiltinTypes.SINT8, arrayAttr.getName() + "[" + indexAttr.hashCode() + "]");
          se = new SelectExpression(arrayAttr, indexAttr, val);
          assert arrayAttr != null;
          assert indexAttr != null;
          assert se != null;

		  if (arrayRef == MJIEnv.NULL) {
		    return ti.createAndThrowException("java.lang.NullPointerException");
		  }


          if ((Integer)cg.getNextChoice()==1) { // check bounds of the index
              pc._addDet(NumericBooleanExpression.create(se.indexExpression, NumericComparator.GE, se.arrayExpression.length));
              if (pc.simplify()) { // satisfiable
                  ((JPCChoiceGenerator) cg).setCurrentPC(pc);

                  return ti.createAndThrowException("java.lang.ArrayIndexOutOfBoundsException", "index greater than array bounds");
              }
              else {
                  ti.getVM().getSystemState().setIgnored(true);
                  return getNext(ti);
              }
          }
          else if ((Integer)cg.getNextChoice()==2) {
              pc._addDet(NumericBooleanExpression.create(se.indexExpression, NumericComparator.LT, Constant.create(BuiltinTypes.SINT32, 0)));
              if (pc.simplify()) { // satisfiable
                  ((JPCChoiceGenerator) cg).setCurrentPC(pc);
                  return ti.createAndThrowException("java.lang.ArrayIndexOutOfBoundsException", "index smaller than array bounds");
              } else {
                  ti.getVM().getSystemState().setIgnored(true);
                  return getNext(ti);
              }
          }
          else {
              pc._addDet(NumericBooleanExpression.create(se.indexExpression, NumericComparator.LT, se.arrayExpression.length));
              pc._addDet(NumericBooleanExpression.create(se.indexExpression, NumericComparator.GE, Constant.create(BuiltinTypes.SINT32, 0)));
              if (pc.simplify()) { //satisfiable
                  ((JPCChoiceGenerator) cg).setCurrentPC(pc);

                  // set the result
                  // We update the Symbolic Array with the get information
                  frame.pop(2); // We pop the array and the index
                  frame.push(0, false);
                  frame.setOperandAttr(val);
                  pc._addDet(se);
                  return getNext(ti);
              }
              else {
                  ti.getVM().getSystemState().setIgnored(true);
                  return getNext(ti);
              }
          }	 
      }
}

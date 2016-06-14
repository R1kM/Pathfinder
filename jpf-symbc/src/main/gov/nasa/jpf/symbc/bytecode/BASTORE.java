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
import gov.nasa.jpf.constraints.expressions.ArrayExpression;
import gov.nasa.jpf.constraints.expressions.Constant;
import gov.nasa.jpf.constraints.expressions.NumericBooleanExpression;
import gov.nasa.jpf.constraints.expressions.NumericComparator;
import gov.nasa.jpf.constraints.expressions.SelectExpression;
import gov.nasa.jpf.constraints.expressions.StoreExpression;
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
 * Store into byte or boolean array
 * ..., arrayref, index, value  => ...
 */
public class BASTORE extends gov.nasa.jpf.jvm.bytecode.BASTORE {

	 @Override
	  public Instruction execute (ThreadInfo ti) {
         // We may need to add the case where we have a smybolic index and a concrete array

          Expression<Integer> indexAttr = null;
          ArrayExpression<Byte> arrayAttr = null;
		  StackFrame frame = ti.getModifiableTopFrame();

          if (peekArrayAttr(ti)==null || !(peekArrayAttr(ti) instanceof ArrayExpression<?>)) {
             //In this case, the array isn't symbolic
             if (peekIndexAttr(ti) == null || !(peekIndexAttr(ti) instanceof Expression<?>)) {
                 if (frame.getOperandAttr(0) == null || !(frame.getOperandAttr(0) instanceof Expression<?>)) {
                     // nothing is symbolic here
                     return super.execute(ti);
                 }
             }
          }


          ChoiceGenerator<?> cg;
          boolean condition;
          int arrayRef = peekArrayRef(ti); // need to be polymorphic, could be LongArrayStore

          if (arrayRef == MJIEnv.NULL) {
              return ti.createAndThrowException("java.lang.NullPointerException");
          }

          if (!ti.isFirstStepInsn()) { // first time around
              cg = new JPCChoiceGenerator(3);
              ((JPCChoiceGenerator) cg).setOffset(this.position);
              ((JPCChoiceGenerator) cg).setMethodName(this.getMethodInfo().getFullName());
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

		 if (peekIndexAttr(ti)==null || !(peekIndexAttr(ti) instanceof Expression<?>)) {
              int index = ti.getTopFrame().peek(1);
              indexAttr =  Constant.create(BuiltinTypes.SINT32, index); 
		  } else {
              indexAttr = Translate.translateInt((Expression<?>)peekIndexAttr(ti));
          }
          assert (indexAttr != null) : "indexAttr shouldn't be null in IASTORE instruction";
  
          if (peekArrayAttr(ti)==null || !(peekArrayAttr(ti) instanceof ArrayExpression<?>)) {
             //In this case, the array isn't symbolic
             if (peekIndexAttr(ti) == null || !(peekIndexAttr(ti) instanceof Expression<?>)) {
                 if (frame.getOperandAttr(0) == null || !(frame.getOperandAttr(0) instanceof Expression<?>)) {
                     // nothing is symbolic here
                     return super.execute(ti);
                 }
             } else {
              // We create a symbolic array out of the concrete array
               ElementInfo arrayInfo = ti.getElementInfo(arrayRef);   
               arrayAttr = ArrayExpression.create(BuiltinTypes.SINT8, arrayInfo.arrayLength());
               // We add the constraints about all the elements of the array
               for (int i = 0; i < arrayInfo.arrayLength(); i++) {
                   byte arrValue = arrayInfo.getByteElement(i);
                   pc._addDet(new SelectExpression(arrayAttr, Constant.create(BuiltinTypes.SINT32, i), Constant.create(BuiltinTypes.SINT8, arrValue)));
               }
             }
          } else {
            arrayAttr = (ArrayExpression<Byte>)peekArrayAttr(ti);
          }
          assert (arrayAttr != null) : "arrayAttr shouldn't be null in IASTORE instruction";

		  if (arrayRef == MJIEnv.NULL) {
		        return ti.createAndThrowException("java.lang.NullPointerException");
		  } 

          
          if ((Integer)cg.getNextChoice() == 1) { // check bounds of the index
              pc._addDet(NumericBooleanExpression.create(indexAttr, NumericComparator.GE, arrayAttr.length));
              if (pc.simplify()) { // satisfiable
                  ((JPCChoiceGenerator) cg).setCurrentPC(pc);
                  return ti.createAndThrowException("java.lang.ArrayIndexOutOfBoundsException", "index greater than array bounds");
              }
              else {
                  ti.getVM().getSystemState().setIgnored(true);
                  return getNext(ti);
              }
          }
          else if ((Integer)cg.getNextChoice() == 2) {
              pc._addDet(NumericBooleanExpression.create(indexAttr, NumericComparator.LT, Constant.create(BuiltinTypes.SINT32, 0)));
              if (pc.simplify()) { // satisfiable
                  ((JPCChoiceGenerator) cg).setCurrentPC(pc);
                  return ti.createAndThrowException("java.lang.ArrayIndexOutOfBoundsException", "index smaller than array bounds");
              }
              else {
                  ti.getVM().getSystemState().setIgnored(true);
                  return getNext(ti);
              }
          }
          else {
              pc._addDet(NumericBooleanExpression.create(indexAttr, NumericComparator.LT, arrayAttr.length));
              pc._addDet(NumericBooleanExpression.create(indexAttr, NumericComparator.GE, Constant.create(BuiltinTypes.SINT32, 0)));
              if (pc.simplify()) { // satisfiable
                  ((JPCChoiceGenerator) cg).setCurrentPC(pc);
                  
                  // set the result                 

                  // We have to check if the value is symbolic or not, create a symbolicIntegerValueatIndex out of it, and 
                  // call the setVal function, before storing the attr 
                  Expression<Byte> sym_value = null;
		          if (frame.getOperandAttr(0) == null || !(frame.getOperandAttr(0) instanceof Expression<?>)) {
                      // The value isn't symbolic. We store a new IntegerConstant in the valAt map, at index indexAttr
                      byte value = (byte)frame.pop();
                      sym_value = Constant.create(BuiltinTypes.SINT8, value);
                  }
                  else {
                      // The value is symbolic.
                      sym_value = Translate.translateIntType((Expression<?>)frame.getOperandAttr(0), BuiltinTypes.SINT8) ;
                      frame.pop();
                  }
                  // We create a new arrayAttr, and inherits information from the previous attribute
                  ArrayExpression<Byte> newArrayAttr = new ArrayExpression<Byte>(arrayAttr);
                  frame.pop(2); // We pop the array and the index

                  StoreExpression se = new StoreExpression(arrayAttr, indexAttr, sym_value, newArrayAttr);
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

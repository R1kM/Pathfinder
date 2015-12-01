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

import gov.nasa.jpf.symbc.SymbolicInstructionFactory;
import gov.nasa.jpf.symbc.arrays.ArrayExpression;
import gov.nasa.jpf.symbc.arrays.IntegerSymbolicArray;
import gov.nasa.jpf.symbc.arrays.SymbolicIntegerValueAtIndex;
import gov.nasa.jpf.symbc.arrays.PreviousIntegerArray;
import gov.nasa.jpf.symbc.arrays.StoreExpression;
import gov.nasa.jpf.symbc.numeric.Comparator;
import gov.nasa.jpf.symbc.numeric.IntegerConstant;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.ArrayIndexOutOfBoundsExecutiveException;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * Store into int array
 * ..., arrayref, index, value => ...
 */
public class IASTORE extends gov.nasa.jpf.jvm.bytecode.IASTORE {

	 

	 @Override
	  public Instruction execute (ThreadInfo ti) {
         // We may need to add the case where we have a smybolic index and a concrete array

         if (peekArrayAttr(ti)==null || !(peekArrayAttr(ti) instanceof ArrayExpression)) {
             //In this case, the array isn't symbolic
             return super.execute(ti);
         }

         IntegerSymbolicArray arrayAttr = (IntegerSymbolicArray)peekArrayAttr(ti);

		 if (peekIndexAttr(ti)==null || !(peekIndexAttr(ti) instanceof IntegerExpression))
             // In this case, the index isn't symbolic
             //TODO Check bounds of array
             // TODO Add Store in PathCondition
			  return super.execute(ti);
		  int arrayref = peekArrayRef(ti); // need to be polymorphic, could be LongArrayStore
		  StackFrame frame = ti.getModifiableTopFrame();
          IntegerExpression indexAttr = (IntegerExpression)peekIndexAttr(ti);
		  if (arrayref == MJIEnv.NULL) {
		        return ti.createAndThrowException("java.lang.NullPointerException");
		  } 

          ChoiceGenerator<?> cg;
          boolean condition;

          if (!ti.isFirstStepInsn()) { // first time around
              cg = new PCChoiceGenerator(3);
              ((PCChoiceGenerator) cg).setOffset(this.position);
              ((PCChoiceGenerator) cg).setMethodName(this.getMethodInfo().getFullName());
              ti.getVM().setNextChoiceGenerator(cg);
              return this;
          } else { // this is what really returns results
            cg = ti.getVM().getChoiceGenerator();
            assert (cg instanceof PCChoiceGenerator) : "expected PCChoiceGenerator, got: " + cg;
          }
          
          PathCondition pc;
          ChoiceGenerator<?> prev_cg = cg.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);
          
          if (prev_cg == null)
              pc = new PathCondition();
          else
              pc = ((PCChoiceGenerator)prev_cg).getCurrentPC();
          
          assert pc != null;
          
          if ((Integer)cg.getNextChoice() == 1) { // check bounds of the index
              pc._addDet(Comparator.GE, indexAttr, arrayAttr.length);
              if (pc.simplify()) { // satisfiable
                  ((PCChoiceGenerator) cg).setCurrentPC(pc);
                  return ti.createAndThrowException("java.lang.ArrayOutOfBoundsException", "index greater than array bounds");
              }
              else {
                  ti.getVM().getSystemState().setIgnored(true);
                  return getNext(ti);
              }
          }
          else if ((Integer)cg.getNextChoice() == 2) {
              pc._addDet(Comparator.LT, indexAttr, new IntegerConstant(0));
              if (pc.simplify()) { // satisfiable
                  ((PCChoiceGenerator) cg).setCurrentPC(pc);
                  return ti.createAndThrowException("java.lang.ArrayOutOfBoundsException", "index smaller than array bounds");
              }
              else {
                  ti.getVM().getSystemState().setIgnored(true);
                  return getNext(ti);
              }
          }
          else {
              pc._addDet(Comparator.LT, indexAttr, arrayAttr.length);
              pc._addDet(Comparator.GE, indexAttr, new IntegerConstant(0));
              if (pc.simplify()) { // satisfiable
                  ((PCChoiceGenerator) cg).setCurrentPC(pc);
                  
                  // set the result                 

                  // We have to check if the value is symbolic or not, create a symbolicIntegerValueatIndex out of it, and 
                  // call the setVal function, before storing the attr 
                  IntegerExpression sym_value = null;
		          if (frame.getOperandAttr(0) == null || !(frame.getOperandAttr(0) instanceof IntegerExpression)) {
                      // The value isn't symbolic. We store a new IntegerConstant in the valAt map, at index indexAttr
                      int value = frame.pop();
                      sym_value = new IntegerConstant(value);
                  }
                  else {
                      // The value is symbolic.
                      sym_value = (IntegerExpression)frame.getOperandAttr(0);
                      frame.pop();
                  }
                  PreviousIntegerArray previous = new PreviousIntegerArray(arrayAttr, indexAttr, sym_value);
                  // We create a new arrayAttr, and inherits information from the previous attribute
                  IntegerSymbolicArray newArrayAttr = new IntegerSymbolicArray(previous);
                  frame.setLocalAttr(newArrayAttr.getSlot(), newArrayAttr);
                  frame.pop(2); // We pop the array and the index

                  StoreExpression se = new StoreExpression(arrayAttr, indexAttr, sym_value);
                  pc._addDet(Comparator.EQ, se, newArrayAttr);

                  return getNext(ti);
             }
             else {
                 ti.getVM().getSystemState().setIgnored(true);
                 return getNext(ti);
             }
          }
      }
	 
}

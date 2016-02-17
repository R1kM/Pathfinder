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
import gov.nasa.jpf.symbc.arrays.HelperResult;
import gov.nasa.jpf.symbc.arrays.ObjectSymbolicArray;
import gov.nasa.jpf.symbc.arrays.SelectExpression;
import gov.nasa.jpf.symbc.heap.HeapChoiceGenerator;
import gov.nasa.jpf.symbc.heap.HeapNode;
import gov.nasa.jpf.symbc.heap.Helper;
import gov.nasa.jpf.symbc.heap.SymbolicInputHeap;
import gov.nasa.jpf.symbc.numeric.Comparator;
import gov.nasa.jpf.symbc.numeric.IntegerConstant;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.ArrayFields;
import gov.nasa.jpf.vm.ArrayIndexOutOfBoundsExecutiveException;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ClassLoaderInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.Scheduler;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * Load reference from array
 * ..., arrayref, index  => ..., value
 */
public class AALOAD extends gov.nasa.jpf.jvm.bytecode.AALOAD {

	
  @Override
  public Instruction execute (ThreadInfo ti) {

      boolean abstractClass = false;
      ObjectSymbolicArray arrayAttr = null;
      HeapNode[] prevSymRefs = null; // previously initialized objects of same type
      int numSymRefs = 0; // number of previously initialized objects
      ChoiceGenerator<?> prevHeapCG = null;
      ChoiceGenerator<?> cg;
      int currentChoice;
      IntegerExpression indexAttr = null;
   	  StackFrame frame = ti.getModifiableTopFrame();
	   arrayRef = frame.peek(1); // ..,arrayRef,idx
	
      if (peekArrayAttr(ti) == null || !(peekArrayAttr(ti) instanceof ArrayExpression)) {
          // In this case, the array isn't symbolic
          if (peekIndexAttr(ti) == null || !(peekIndexAttr(ti) instanceof IntegerExpression)) {
              // In this case, the index isn't symbolic either
              return super.execute(ti);
          }
          // We have a concrete array, but a symbolic index. We add all the constraints about the elements of the array and perform the select
          // We will need to get information about the type of the elements as well
          // We need to add the information in PC after it is declared.
          // TODO
          // Make a PCChoiceGenerator on all the possible indices. Return the object at array[i] for each i
          ElementInfo arrayInfo = ti.getElementInfo(arrayRef);
          if (!ti.isFirstStepInsn()) { // first time around
              cg = new PCChoiceGenerator(arrayInfo.arrayLength() + 2);
              ((PCChoiceGenerator)cg).setOffset(this.position);
              ((PCChoiceGenerator)cg).setMethodName(this.getMethodInfo().getFullName());
              ti.getVM().setNextChoiceGenerator(cg);
              return this;
          } else { // this is what really returns results
            cg = ti.getVM().getLastChoiceGeneratorOfType(PCChoiceGenerator.class);
            assert (cg instanceof PCChoiceGenerator) : "expected PCChoiceGenerator, got: " + cg;
            currentChoice = (Integer)cg.getNextChoice();
          }

          PathCondition pc;
          ChoiceGenerator<?> prev_cg = cg.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);

          if (prev_cg == null)
              pc = new PathCondition();
          else
              pc = ((PCChoiceGenerator)prev_cg).getCurrentPC();

          assert pc != null;
          indexAttr = (IntegerExpression)peekIndexAttr(ti);

          if (currentChoice < arrayInfo.arrayLength()) {
            // For each possible index, we check if the symbolic index can be equal to it. If so, we return the value at this index

            pc._addDet(Comparator.EQ, indexAttr, new IntegerConstant(currentChoice));
            if (pc.simplify()) { // satisfiable
                frame.pop(2);
                arrayInfo.checkArrayBounds(currentChoice); // should not fail
                int value = arrayInfo.getReferenceElement(currentChoice);
                frame.pushRef(value);
                Object elementAttr = arrayInfo.getElementAttr(currentChoice);
                if (elementAttr != null) {
                    frame.setOperandAttr(elementAttr);
                }
                return getNext(ti);
            } else {
                ti.getVM().getSystemState().setIgnored(true);
                return getNext(ti);
            }
          } else if (currentChoice == arrayInfo.arrayLength()) {
            pc._addDet(Comparator.GE, indexAttr, new IntegerConstant(arrayInfo.arrayLength()));
            if (pc.simplify()) { // satisfiable
                ((PCChoiceGenerator) cg).setCurrentPC(pc);
                return ti.createAndThrowException("java.lang.ArrayIndexOutOfBoundsException", "index greater than array bounds");
            } else {
                ti.getVM().getSystemState().setIgnored(true);
                return getNext(ti);
            }
          } else if (currentChoice == arrayInfo.arrayLength() +1) {
              pc._addDet(Comparator.LT, indexAttr, new IntegerConstant(0));
              if (pc.simplify()) { // satisfiable
                  ((PCChoiceGenerator) cg).setCurrentPC(pc);
                  return ti.createAndThrowException("java.lang.ArrayIndexOutOfBoundsException", "index smaller than array bounds");
              } else {
                  ti.getVM().getSystemState().setIgnored(true);
                  return getNext(ti);
              }
          } else {
              throw new RuntimeException("We shouldn't end here in AALOAD");
          }
       } else {
           arrayAttr = (ObjectSymbolicArray)peekArrayAttr(ti);
       }
	  
      String typeElemArray = arrayAttr.getElemType();

      if (typeElemArray.equals("?")) {
            throw new RuntimeException("Type of array elements unknown");
      }

      ClassInfo typeClassInfo = ClassLoaderInfo.getCurrentResolvedClassInfo(typeElemArray);

      ChoiceGenerator<?> thisHeapCG;

    // TODO Replace HeapChoiceGenerator by a PCChoiceGenerator, and mimick concrete case behaviour
    // index in bounds will be detected by PCChoiceGenerator

      if (!ti.isFirstStepInsn()) { // first time around
          prevSymRefs = null;
          numSymRefs = 0;
          prevHeapCG = ti.getVM().getLastChoiceGeneratorOfType(HeapChoiceGenerator.class);

          if (prevHeapCG != null) {
              // determine number of previously initilaized objects
              SymbolicInputHeap symInputHeap = 
                    ((HeapChoiceGenerator)prevHeapCG).getCurrentSymInputHeap();
              prevSymRefs = symInputHeap.getNodesOfType(typeClassInfo);
              numSymRefs = prevSymRefs.length;
          }
          int increment = 2;
          if (typeClassInfo.isAbstract()) {
               abstractClass =true;
               increment = 1;
          }

          thisHeapCG = new HeapChoiceGenerator(numSymRefs + increment + 2); // Number of prev. init. objects + 2 conditions on index in bounds
          ti.getVM().setNextChoiceGenerator(thisHeapCG);
          return this;
      } else { // this is what really returns results
        thisHeapCG = ti.getVM().getChoiceGenerator();
        assert (thisHeapCG instanceof HeapChoiceGenerator) : "expected HeapChoiceGenerator, got: " + thisHeapCG;
        currentChoice = ((HeapChoiceGenerator) thisHeapCG).getNextChoice();
      }

      PathCondition pcHeap;
      SymbolicInputHeap symInputHeap;

      prevHeapCG = thisHeapCG.getPreviousChoiceGeneratorOfType(HeapChoiceGenerator.class);

      if (prevHeapCG == null) {
          pcHeap = new PathCondition();
          symInputHeap = new SymbolicInputHeap();
      } else {
          pcHeap = ((HeapChoiceGenerator)prevHeapCG).getCurrentPCheap();
          symInputHeap = ((HeapChoiceGenerator) prevHeapCG).getCurrentSymInputHeap();
      }

      assert pcHeap != null;
      assert symInputHeap != null;

       SelectExpression se = null;


       if (peekIndexAttr(ti) == null || !(peekIndexAttr(ti) instanceof IntegerExpression)) {
           // In this case, the index isn't symbolic
           index = frame.peek();
           se = new SelectExpression(arrayAttr, index);
           indexAttr = new IntegerConstant(index);
       } else {
           indexAttr = (IntegerExpression)peekIndexAttr(ti);
           se = new SelectExpression(arrayAttr, indexAttr);
       }

       assert arrayAttr != null;
       assert indexAttr != null;
       assert se != null;

	    if (arrayRef == MJIEnv.NULL) {
	      return ti.createAndThrowException("java.lang.NullPointerException");
	    }
	  
       int daIndex = 0; // index into JPF's dynamic area
       // TODO We should have a different strategy : 
       // We can either load one of the objects previously loaded from this array (add a field in ObjectSymbolicArray)
       // Or do lazy initialization (null / new object)
       // The implementation first case is similar to the concrete case

       if (currentChoice < numSymRefs) { // using a previously initialized object
           pcHeap._addDet(Comparator.LT, se.index, se.ae.length);
           pcHeap._addDet(Comparator.GE, se.index, new IntegerConstant(0));
           if (pcHeap.simplify()) { // satisfiable
               HeapNode candidateNode = prevSymRefs[currentChoice];
               pcHeap._addDet(Comparator.EQ, se, candidateNode.getSymbolic());
               daIndex = candidateNode.getIndex();
               frame.pop(2); // We pop the array and the index
               frame.push(daIndex, true); // We have instantiated an object here, and added the constriants in the PC

               ((HeapChoiceGenerator)thisHeapCG).setCurrentPCheap(pcHeap);
               ((HeapChoiceGenerator)thisHeapCG).setCurrentSymInputHeap(symInputHeap);
               return getNext(ti);
           } else {
               ti.getVM().getSystemState().setIgnored(true);
               return getNext(ti);
           }
        } else if (currentChoice == numSymRefs) { // check bounds of the index
            pcHeap._addDet(Comparator.GE, se.index, se.ae.length);
            if (pcHeap.simplify()) { // satisfiable
                ((HeapChoiceGenerator) thisHeapCG).setCurrentPCheap(pcHeap);
                return ti.createAndThrowException("java.lang.ArrayIndexOutOfBoundsException", "index greater than array bounds");
            } else {
                ti.getVM().getSystemState().setIgnored(true);
                return getNext(ti);
            }
        } else if (currentChoice == (numSymRefs + 1)) {
            pcHeap._addDet(Comparator.LT, se.index, new IntegerConstant(0));
            if (pcHeap.simplify()) { // satisfiable
                ((HeapChoiceGenerator) thisHeapCG).setCurrentPCheap(pcHeap);
                return ti.createAndThrowException("java.lang.ArrayIndexOutOfBoundsException", "index smaller than array bounds");
            } else {
                ti.getVM().getSystemState().setIgnored(true);
                return getNext(ti);
            }
        } else if (currentChoice == (numSymRefs + 2)) { // null object
            pcHeap._addDet(Comparator.LT, se.index, se.ae.length);
            pcHeap._addDet(Comparator.GE, se.index, new IntegerConstant(0));
            if (pcHeap.simplify()) { // satisfiable
                pcHeap._addDet(Comparator.EQ, se, new IntegerConstant(-1));
                daIndex = -1;
                frame.pop(2); // We pop the array and the index;
                frame.push(daIndex, true);

                ((HeapChoiceGenerator)thisHeapCG).setCurrentPCheap(pcHeap);
                ((HeapChoiceGenerator)thisHeapCG).setCurrentSymInputHeap(symInputHeap);
                return getNext(ti);
            } else {
                ti.getVM().getSystemState().setIgnored(true);
                return getNext(ti);
            }
        } else if (currentChoice == (numSymRefs + 3)) {
            pcHeap._addDet(Comparator.LT, se.index, se.ae.length);
            pcHeap._addDet(Comparator.GE, se.index, new IntegerConstant(0));
            if (pcHeap.simplify()) { // satisfiable
                HelperResult hpResult = Helper.addNewArrayHeapNode(typeClassInfo, ti, arrayAttr, pcHeap, symInputHeap, numSymRefs, prevSymRefs, false);
                daIndex = hpResult.idx;
                HeapNode candidateNode = hpResult.n;
                pcHeap._addDet(Comparator.EQ, se, candidateNode.getSymbolic());
                frame.pop(2); // We pop the array and the index
                frame.push(daIndex, true);

                ((HeapChoiceGenerator)thisHeapCG).setCurrentPCheap(pcHeap);
                ((HeapChoiceGenerator)thisHeapCG).setCurrentSymInputHeap(symInputHeap);
                return getNext(ti);
            } else {
                ti.getVM().getSystemState().setIgnored(true);
                return getNext(ti);
            }
        }   
        throw new RuntimeException("This point is unreachable");
    }
}

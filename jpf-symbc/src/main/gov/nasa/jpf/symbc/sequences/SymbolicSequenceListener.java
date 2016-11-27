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
// Administrator of the National Aeronautics and Space Administration
// (NASA).  All Rights Reserved.
//
// This software is distributed under the NASA Open Source Agreement
// (NOSA), version 1.3.  The NOSA has been approved by the Open Source
// Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
// directory tree for the complete NOSA document.
//
// THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
// KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
// LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
// SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
// A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
// THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
// DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
package gov.nasa.jpf.symbc.sequences;

// does not work well for static methods:summary not printed for errors
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.Property;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.vm.ChoiceGenerator;


import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ClassLoaderInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NoUncaughtExceptionsProperty;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.SystemState;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Types;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.BooleanFieldInfo;
import gov.nasa.jpf.vm.DoubleFieldInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.FloatFieldInfo;
import gov.nasa.jpf.vm.IntegerFieldInfo;
import gov.nasa.jpf.vm.LongFieldInfo;
import gov.nasa.jpf.vm.ReferenceFieldInfo;

import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.jvm.bytecode.JVMReturnInstruction;

import gov.nasa.jpf.report.ConsolePublisher;
import gov.nasa.jpf.report.Publisher;
import gov.nasa.jpf.report.PublisherExtension;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.SymbolicInstructionFactory;
import gov.nasa.jpf.symbc.arrays.ArrayExpression;
import gov.nasa.jpf.symbc.arrays.SelectExpression;
import gov.nasa.jpf.symbc.bytecode.BytecodeUtils;
import gov.nasa.jpf.symbc.bytecode.INVOKESTATIC;
import gov.nasa.jpf.symbc.concolic.PCAnalyzer;
import gov.nasa.jpf.symbc.heap.HeapChoiceGenerator;
import gov.nasa.jpf.symbc.heap.HeapNode;
import gov.nasa.jpf.symbc.heap.SymbolicInputHeap;
import gov.nasa.jpf.symbc.numeric.Constraint;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.symbc.numeric.RealExpression;
import gov.nasa.jpf.symbc.numeric.SymbolicInteger;

import gov.nasa.jpf.symbc.string.StringSymbolic;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import static java.lang.Math.toIntExact;

/**
 *
 *
 * @author Mithun Acharya
 * with inputs from Corina.
 *
 * Note that all the methods of interest should be specified in +symbolic.method option.
 * if a method is not specified in +symbolic.method it will not be printed.
 * even if the method, foo(int i) is invoked concretely always, we should have
 * foo(con) in +symbolic.method option
 *
 * Algorithm (Works independent of search order):
 *
 * When instructionExecuted->JVMInvokeInstruction, remember the executed method, symbolic attributes, etc.
 * in a SequenceChoiceGenerator
 *
 * The main idea is to exploit the fact that
 * "at each state, the path from start state to the current state has a
 * unique chain of choice generators"
 *
 * During stateBacktracked or propertyViolated, get the chain of choice generators. In this chain look
 * for SequenceChoiceGenerators (which hold information about symbolically executed methods).
 * With the current path condition solution and the symbolic attributes
 * stored in SequenceChoiceGenerators, output the concrete method sequence.
 *
 *
 * KNOWN PROBLEMS:
 *
 * 1) For JUnit test cases, getting class name and object name is not smart.
 *
 *
 */
public class SymbolicSequenceListener extends PropertyListenerAdapter implements PublisherExtension {


	// this set will store all the method sequences.
	// will be printed at last.
	// 'methodSequences' is a set of 'methodSequence's
	// A single 'methodSequence' is a vector of invoked 'method's along a path
	// A single invoked 'method' is represented as a String.
 	Set<Vector> methodSequences = new LinkedHashSet<Vector>();

 	// Name of the class under test
 	String className ="";

 	// custom marker to mark error strings in method sequences
 	private final static String exceptionMarker = "##EXCEPTION## ";
    private static HashMap<Integer, String> refMap = new HashMap<Integer, String>();

	public SymbolicSequenceListener(Config conf, JPF jpf) {
		jpf.addPublisherExtension(ConsolePublisher.class, this);
	}

	@Override
	public void propertyViolated (Search search){
		System.out.println("--------->property violated");
		VM vm = search.getVM();
		SystemState ss = vm.getSystemState();
		ChoiceGenerator<?> cg = vm.getChoiceGenerator();
		if (!(cg instanceof PCChoiceGenerator)){
			ChoiceGenerator<?> prev_cg = cg.getPreviousChoiceGenerator();
			while (!((prev_cg == null) || (prev_cg instanceof PCChoiceGenerator))) {
				prev_cg = prev_cg.getPreviousChoiceGenerator();
			}
			cg = prev_cg;
		}
		Property prop = search.getLastError().getProperty();
		String errAnn="";
		if (prop instanceof NoUncaughtExceptionsProperty) {
			String exceptionClass=((NoUncaughtExceptionsProperty)prop).getUncaughtExceptionInfo().getExceptionClassname();
			errAnn = "(expected = "+ exceptionClass +".class)";
		}

		String error = search.getLastError().getDetails();
		error = "\"" + error.substring(0,error.indexOf("\n")) + "...\"";

		if ((cg instanceof PCChoiceGenerator) &&
				      ((PCChoiceGenerator) cg).getCurrentPC() != null){

			PathCondition pc = ((PCChoiceGenerator) cg).getCurrentPC().make_copy();
            HeapChoiceGenerator heapCG = vm.getLastChoiceGeneratorOfType(HeapChoiceGenerator.class);
            PathCondition heapPC = (heapCG==null ? new PathCondition() : heapCG.getCurrentPCheap().make_copy());
            pc.appendPathcondition(heapPC);
			System.out.println("pc "+ pc.count() + " "+pc);

			//solve the path condition
            /* TODO : Temp comment before update concolic module
			if (SymbolicInstructionFactory.concolicMode) { //TODO: cleaner
				SymbolicConstraintsGeneral solver = new SymbolicConstraintsGeneral();
				PCAnalyzer pa = new PCAnalyzer();
				pa.solve(pc,solver);
			}
			else */
			Map<String, Object> val = pc.solveWithValuation();

			// get the chain of choice generators.
			ChoiceGenerator<?> [] cgs = ss.getChoiceGenerators();
			Vector<String> methodSequence = getMethodSequence(cgs, val, pc, heapCG, ThreadInfo.getCurrentThread());
			// Now append the error String and then add methodSequence to methodSequences
			// prefix the exception marker to distinguish this from
			// an invoked method.
			if (errAnn!="")
				methodSequence.add(0,errAnn);
			methodSequence.add(exceptionMarker + error);
			methodSequences.add(methodSequence);
		}
	}

    public void stateAdvanced (Search search) {
        if (search.isEndState()) {
                VM vm = search.getVM();
			    SystemState ss = vm.getSystemState();
        		ThreadInfo ti = ThreadInfo.getCurrentThread(); 
        
        		ChoiceGenerator<?> cg = vm.getChoiceGenerator();
        
        		if (!(cg instanceof PCChoiceGenerator)){
        			ChoiceGenerator<?> prev_cg = cg.getPreviousChoiceGenerator();
        			while (!((prev_cg == null) || (prev_cg instanceof PCChoiceGenerator))) {
        					prev_cg = prev_cg.getPreviousChoiceGenerator();
        			}
        			cg = prev_cg;
        		}
        
        		if ((cg instanceof PCChoiceGenerator) &&
        			      ((PCChoiceGenerator) cg).getCurrentPC() != null){
        
        			PathCondition pc = ((PCChoiceGenerator) cg).getCurrentPC().make_copy();
                    HeapChoiceGenerator heapCG = vm.getLastChoiceGeneratorOfType(HeapChoiceGenerator.class);
                    PathCondition heapPC = (heapCG==null ? new PathCondition() : heapCG.getCurrentPCheap().make_copy());
                    pc.appendPathcondition(heapPC);
        			//solve the path condition
                    /* TODO : Comment until update of concolic module with jconstraints
        			if (SymbolicInstructionFactory.concolicMode) { //TODO: cleaner
        				SymbolicConstraintsGeneral solver = new SymbolicConstraintsGeneral();
        				PCAnalyzer pa = new PCAnalyzer();
        				pa.solve(pc,solver);
        			}
        			else */
        				Map<String, Object> val = pc.solveWithValuation();
                        System.out.println(pc);
        			// get the chain of choice generators.
        			ChoiceGenerator<?> [] cgs = ss.getChoiceGenerators();
        			methodSequences.add(getMethodSequence(cgs, val, pc, heapCG, ti));
        		}
			}
    }


	@Override
	 public void instructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction, Instruction executedInstruction) {



		if (!vm.getSystemState().isIgnored()) {
			Instruction insn = executedInstruction;
			SystemState ss = vm.getSystemState();
			ThreadInfo ti = currentThread;
			Config conf  = vm.getConfig();

			if (insn instanceof JVMInvokeInstruction && insn.isCompleted(ti)) {
				JVMInvokeInstruction md = (JVMInvokeInstruction) insn;
				String methodName = md.getInvokedMethodName();
				int numberOfArgs = md.getArgumentValues(ti).length;
				MethodInfo mi = md.getInvokedMethod();

				StackFrame sf = ti.getTopFrame();
				String shortName = methodName;
				//String longName = mi.getLongName();
				if (methodName.contains("("))
					shortName = methodName.substring(0,methodName.indexOf("("));
				// does not work for recursive invocations of sym methods; should compare MethodInfo instead
				//if(!shortName.equals(sf.getMethodName()))
					//return;
				if(!mi.equals(sf.getMethodInfo()))
					return;

				if ((BytecodeUtils.isMethodSymbolic(conf, mi.getFullName(), numberOfArgs, null))){

					// FIXME: get the object name?
					// VirtualInvocation virtualInvocation = (VirtualInvocation)insn;
					// int ref = virtualInvocation.getThis(ti);
					// DynamicElementInfo d = ss.ks.da.get(ref);
					// right now I am just getting the class name
					className = mi.getClassName();


					// get arg values
					Object [] argValues = md.getArgumentValues(ti);

					// get symbolic attributes
					// concretely executed method will have null attributes.
					// TODO: fix there

					byte[] argTypes = mi.getArgumentTypes();
					Object[] attributes = new Object[numberOfArgs];

					int count = 1 ; // we do not care about this
					if (md instanceof INVOKESTATIC)
						count = 0;  //no "this" reference
					for (int i = 0; i < numberOfArgs; i++) {
						attributes[i] = sf.getLocalAttr(count);
						count++;
						if(argTypes[i]== Types.T_LONG || argTypes[i] == Types.T_DOUBLE)
							count++;
					}

					// Create a new SequenceChoiceGenerator.
					// this is just to store the information
					// regarding the executed method.
					SequenceChoiceGenerator cg = new SequenceChoiceGenerator(shortName);
                    cg.setArgTypes(mi.getArgumentTypeNames());
					cg.setArgValues(argValues);
					cg.setArgAttributes(attributes);
					// Does not actually make any choice
					ss.setNextChoiceGenerator(cg);
					// nothing to do as there are no choices.
				}
			} /*
			else if (insn instanceof JVMReturnInstruction){
        		//ThreadInfo ti = vm.getChoiceGenerator().getThreadInfo();
        		MethodInfo mi = insn.getMethodInfo();
        		String methodName = mi.getFullName();
        
        		int numberOfArgs = mi.getNumberOfArguments();//mi.getArgumentsSize()- 1;// corina: problem here? - 1;
        
        	//	if (BytecodeUtils.isMethodSymbolic(conf, methodName, numberOfArgs, null)){
        
        			ChoiceGenerator<?> cg = vm.getChoiceGenerator();
        
        			if (!(cg instanceof PCChoiceGenerator)){
        				ChoiceGenerator<?> prev_cg = cg.getPreviousChoiceGenerator();
        				while (!((prev_cg == null) || (prev_cg instanceof PCChoiceGenerator))) {
        						prev_cg = prev_cg.getPreviousChoiceGenerator();
        				}
        				cg = prev_cg;
        			}
        
        			if ((cg instanceof PCChoiceGenerator) &&
        				      ((PCChoiceGenerator) cg).getCurrentPC() != null){
        
        				PathCondition pc = ((PCChoiceGenerator) cg).getCurrentPC().make_copy();
                        HeapChoiceGenerator heapCG = vm.getLastChoiceGeneratorOfType(HeapChoiceGenerator.class);
                        PathCondition heapPC = (heapCG==null ? new PathCondition() : heapCG.getCurrentPCheap().make_copy());
                        if (heapPC.header != null)
                            pc.prependAllConjuncts(heapPC.header);
        				//solve the path condition
                        /* TODO : Comment until update of concolic module with jconstraints
        				if (SymbolicInstructionFactory.concolicMode) { //TODO: cleaner
        					SymbolicConstraintsGeneral solver = new SymbolicConstraintsGeneral();
        					PCAnalyzer pa = new PCAnalyzer();
        					pa.solve(pc,solver);
        				}
        				else 
        					Map<String, Object> val = pc.solveWithValuation();
                            System.out.println(pc);
        				// get the chain of choice generators.
        				ChoiceGenerator<?> [] cgs = ss.getChoiceGenerators();
        				methodSequences.add(getMethodSequence(cgs, val, pc, heapCG, ti));
        			}
			} */
		}

	}


    private static Object evaluate (String name, Map<String, Object> val) {
        if (val.containsKey(name)) {
            return val.get(name);
        } else {
            return 0;
        }
    }

    private static int evaluate_object (String name, Map<String, Object> val) {
        if (val.containsKey(name)) {
            return toIntExact((long)val.get(name));
        } else {
            return -1;
        }
    }

	/**
	 *
	 * traverses the ChoiceGenerator chain to get the method sequence
	 * looks for SequenceChoiceGenerator in the chain
	 * SequenceChoiceGenerators have information about the methods
	 * executed and hence the method sequence can be obtained.
	 * A single 'methodSequence' is a vector of invoked 'method's along a path
	 * A single invoked 'method' is represented as a String.
	 *
	 */
	private Vector<String> getMethodSequence(ChoiceGenerator [] cgs, Map<String, Object> val, PathCondition pc, HeapChoiceGenerator heapCG, ThreadInfo ti){
		// A method sequence is a vector of strings
		Vector<String> methodSequence = new Vector<String>();
		ChoiceGenerator cg = null;
		// explore the choice generator chain - unique for a given path.
		for (int i=0; i<cgs.length; i++){
			cg = cgs[i];
			if ((cg instanceof SequenceChoiceGenerator)){
				methodSequence.add(getInvokedMethod((SequenceChoiceGenerator)cg, val, pc, heapCG, ti));
			}
		}
		return methodSequence;
	}

    private static int checkArrayRoot(String exprName, String name) {
        if (exprName.indexOf("!") == -1) {
            if (exprName.equals(name)) {
                return 0;
            } else {
                return -1;
            }
        } else {
            if (exprName.substring(0, exprName.indexOf("!")).equals(name)) {
                return Integer.parseInt(exprName.substring(exprName.indexOf("!") + 1));
            } else {
                return -1;
            }
        }
    }

    private static String translateConstructedArray(int[] arr) {
        if (arr.length == 0) {
            return "";
        }
        String solution = "{";
        for (int i = 0; i < arr.length; i++) {
            solution = solution + arr[i] + ", ";
        }
        return (solution.substring(0, solution.length()-2) + "}");
    }

    private static String translateConstructedReferenceArray(int[] arr) {
        if (arr.length == 0) {
            return "";
        }
        String solution = "{";
        return solution;
    }

    private static String generateArrayElements(PathCondition pc, ArrayExpression e, Map<String, Object> val) {
        // We have an array of primitive type
        if (pc == null || pc.header == null) {
            return "";
        }
        String arrayName = e.getName();
        int[] constructedArray = new int[toIntExact(e.length.solution())];
        int[] constructedRank = new int[toIntExact(e.length.solution())];
        for (int i = 0; i< toIntExact(e.length.solution()); i++) {
            constructedRank[i] = -1;
        }
        Constraint header = pc.header;
        while (header != null) {
            if (header.getLeft() instanceof SelectExpression) {
                SelectExpression currCst = (SelectExpression)header.getLeft();
                int currRank = checkArrayRoot(currCst.arrayExpression.getName(), arrayName);
                if (currRank != -1) {
                    int currIndex = toIntExact(((IntegerExpression)currCst.indexExpression).solution());
                    if (currRank <= constructedRank[currIndex] || constructedRank[currIndex] == -1) {
                        int value = toIntExact(((IntegerExpression)header.getRight()).solution());
                        constructedArray[currIndex] = value;
                        constructedRank[currIndex] = currRank;
                    }
                }
            }
            header = header.and;
        }
        return translateConstructedArray(constructedArray);
    }

    private String generateReferenceArray(PathCondition pc, ArrayExpression e, Map<String, Object> val, String type) {
        // We have an array of reference type
        String result ="        " + type + " " + e.getName() + " = new " + type.substring(0, type.length() -1) + e.length.solution() + "]\n" ;
        if (pc == null || pc.header == null) {
            return result; 
        }
        String arrayName = e.getName();
        int[] constructedArray = new int[toIntExact(e.length.solution())];
        int[] constructedRank = new int[toIntExact(e.length.solution())];
        for (int i = 0; i< toIntExact(e.length.solution()); i++) {
            constructedRank[i] = -1;
        }
        Constraint header = pc.header;
        while (header != null) {
            if (header.getLeft() instanceof SelectExpression) {
                SelectExpression currCst = (SelectExpression)header.getLeft();
                int currRank = checkArrayRoot(currCst.arrayExpression.getName(), arrayName);
                if (currRank != -1) {
                    int currIndex = toIntExact(((IntegerExpression)currCst.indexExpression).solution());
                    if (currRank <= constructedRank[currIndex] || constructedRank[currIndex] == -1) {
                        int value = toIntExact(((IntegerExpression)header.getRight()).solution());
                        constructedArray[currIndex] = value;
                        constructedRank[currIndex] = currRank;
                    }
                }
            }
            header = header.and;
        }
        return result;

    }

    private String generatePrimitiveFields(HeapNode currentNode, ThreadInfo ti, Map<String, Object> val, SymbolicInputHeap symInputHeap, PathCondition pc) {
        String generatedFields = "";
        ElementInfo eiRef = ti.getModifiableElementInfo(currentNode.getIndex());
        for (int fieldIndex = 0; fieldIndex < eiRef.getNumberOfFields(); fieldIndex++) {
            FieldInfo field = eiRef.getFieldInfo(fieldIndex);
            String fullName = currentNode.getSymbolic().getName() + "." + field.getName();
		    if (field instanceof IntegerFieldInfo || field instanceof LongFieldInfo || field instanceof BooleanFieldInfo || field instanceof FloatFieldInfo || field instanceof DoubleFieldInfo) {
                // The field has a numeric type, and was previoulsy initialized
                generatedFields = generatedFields + "		" + fullName + " = " + evaluate(fullName, val) + ";\n";
            }
            if (field instanceof ReferenceFieldInfo) {
                if (((ReferenceFieldInfo)field).getType().contains("[]")) {
                   if (!(pc.arrayExpressions.containsKey(fullName))) {
                        generatedFields = generatedFields + "		" + fullName + " = new " + field.getType() + "{};\n";
                   }
                   else {
                    ArrayExpression e = pc.arrayExpressions.get(fullName);
                    if (e.getElemType().equals("?")) {
                        String arrayElems = generateArrayElements(pc, (ArrayExpression)e, val);
                        if (arrayElems != "") {
                            generatedFields = generatedFields + "        " + fullName + " = new "+field.getType() + arrayElems;
                        } else {
                            generatedFields = generatedFields + "        " + fullName + " = new " + field.getType().substring(0, field.getType().length() -1) +e.length.solution() + "]" ;
                        }
                    } else {
                        generatedFields += generateReferenceArray(pc, (ArrayExpression)e, val, field.getType());
                        generatedFields = generatedFields + "		" + fullName + " = " + e.getName() + ";\n";
                    }
                   }
                } else {
                    // Objects are handled differently
                    ClassInfo typeClassInfo = ClassLoaderInfo.getCurrentResolvedClassInfo(field.getType());
                    HeapNode[] prevSymRefs = symInputHeap.getNodesOfType(typeClassInfo); 
                    int currentRef = evaluate_object(fullName, val);
                    if (prevSymRefs.length == 0 || (currentRef == -1)) {
                        generatedFields = generatedFields + "        " + fullName + " = null;\n";
                    } else {
                        HeapNode currNode = prevSymRefs[prevSymRefs.length -1 - currentRef];
                        int currentIndex = currNode.getIndex();
                        if (refMap.containsKey(currentIndex)) {
                            String fieldName = refMap.get(currentIndex);
                            generatedFields = generatedFields + "        " + fullName + " = "+fieldName+ ";\n";
                        } else {
                            String fieldName = fullName.replaceAll("[.]", "_");
                            String clsName = currNode.getType().getName();
                            generatedFields = generatedFields + "        " + clsName + " " + fieldName + " = new " + clsName + "();\n"; 
                            generatedFields = generatedFields + "        " + fullName + " = "+fieldName+ ";\n";
                            refMap.put(currNode.getIndex(), fieldName);
                            generatedFields += generatePrimitiveFields(currNode, ti, val, symInputHeap, pc);
          //                  throw new RuntimeException("Non-null reference fields not yet generated"); 
                        }
                    }
                }
            }
            // TODO StringHandler
        }
        return generatedFields;
    }

    private String generateObject(ThreadInfo ti, Map<String, Object> val, SymbolicInputHeap symInputHeap, ClassInfo typeClassInfo, IntegerExpression e, PathCondition pc) {
        String createdObjects = "";
        HeapNode[] prevSymRefs = symInputHeap.getNodesOfType(typeClassInfo); 

        HeapNode currentHeapNode = prevSymRefs[prevSymRefs.length - 1 - toIntExact((long)evaluate(((SymbolicInteger)e).getName(), val))];
        if (refMap.containsKey(currentHeapNode.getIndex())) {
            createdObjects += refMap.get(currentHeapNode.getIndex());
        } else {
            String clsName = (currentHeapNode.getType().getName());
            createdObjects += "		" + clsName + " " + ((SymbolicInteger)e).getName() + " = new " + clsName + "();\n";
            refMap.put(currentHeapNode.getIndex(), ((SymbolicInteger)e).getName());
            createdObjects += generatePrimitiveFields(currentHeapNode, ti, val, symInputHeap, pc);
        }
        return createdObjects;
    }

	/**
	 * A single invoked 'method' is represented as a String.
	 * information about the invoked method is got from the SequenceChoiceGenerator
	 */
	private String getInvokedMethod(SequenceChoiceGenerator cg, Map<String, Object> val, PathCondition pc, HeapChoiceGenerator heapCG, ThreadInfo ti){
		String invokedMethod = "";
        String createdObjects = "";
        refMap = new HashMap<Integer, String>();

		// get method name
		String shortName = cg.getMethodShortName();
		invokedMethod +=  shortName + "(";

		// get argument values
		Object[] argValues = cg.getArgValues();

		// get number of arguments
		int numberOfArgs = argValues.length;

		// get symbolic attributes
		Object[] attributes = cg.getArgAttributes();

        String[] argTypes = cg.getArgTypes();

		// get the solution
		for(int i=0; i<numberOfArgs; i++){
			Object attribute = attributes[i];
			if (attribute != null){ // parameter symbolic
				// here we should consider different types of symbolic arguments
				//IntegerExpression e = (IntegerExpression)attributes[i];
				Object e = attributes[i];
				String solution = "";


                if (e instanceof ArrayExpression) {
                    if (((ArrayExpression)e).getElemType().equals("?")) {
                        String arrayElems = generateArrayElements(pc, (ArrayExpression)e, val);
                        if (arrayElems != "") {
                            solution = solution + "new "+argTypes[i] + arrayElems;
                        } else {
                            solution = solution + "new " + argTypes[i].substring(0, argTypes[i].length() -1) +((ArrayExpression)e).length.solution() + "]" ;
                        }
                    } else {
                        createdObjects += generateReferenceArray(pc, (ArrayExpression)e, val, argTypes[i]);
                        solution += ((ArrayExpression)e).getName();
                    }
                }
				else if(e instanceof IntegerExpression) {
                    if (argValues[i].toString().indexOf("@") != -1) {
                        // We have an object, not a primitive type
                        if (evaluate_object(((SymbolicInteger)e).getName(), val) == -1) {
                            solution = solution+"null";
                        } else {
                          ClassInfo typeClassInfo = ClassLoaderInfo.getCurrentResolvedClassInfo(argTypes[i]);
                          assert (heapCG != null) : "HeapCG should not be null if we have a symbolic object"; 
                          SymbolicInputHeap symInputHeap = heapCG.getCurrentSymInputHeap();
                          assert symInputHeap != null;
                          createdObjects += generateObject(ti, val, symInputHeap, typeClassInfo, ((SymbolicInteger)e), pc);
                          solution = solution+((SymbolicInteger)e).getName();
                       } 
                    } 
					// trick to print bools correctly
					else if(argValues[i].toString()=="true" || argValues[i].toString()=="false") {
						if(((IntegerExpression)e).solution() == 0)
							solution = solution+ "false";
						else
							solution = solution+ "true";
					}
					else
						solution = solution+ (((IntegerExpression)e).solution());
				}
                else if (e instanceof RealExpression) 
                    solution = solution+ ((RealExpression) e).solution();
				else
					solution = solution+ ((StringSymbolic) e).solution();
				invokedMethod += solution + ",";
			}
			else { // parameter concrete - for a concrete parameter, the symbolic attribute is null
				invokedMethod += argValues[i] + ",";
			}
		}

		// remove the extra comma
		if (invokedMethod.endsWith(","))
			invokedMethod = invokedMethod.substring(0,invokedMethod.length()-1);
		invokedMethod += ")";

		String objectName = (className.toLowerCase()).replace(".", "_");
        invokedMethod = "		" +  objectName + "." + invokedMethod + ";";

		return createdObjects + invokedMethod;
	}



      //	-------- the publisher interface
	public void publishFinished (Publisher publisher) {


		PrintWriter pw = publisher.getOut();
		// here just print the method sequences
		publisher.publishTopicStart("Method Sequences");
		printMethodSequences(pw);

		// print JUnit4.0 test class
		publisher.publishTopicStart("JUnit 4.0 test class");
		printJUnitTestClass(pw);

	}


	  /**
	   * @author Mithun Acharya
	   *
	   * prints the method sequences
	   */
	  private void printMethodSequences(PrintWriter pw){
		  Iterator<Vector> it = methodSequences.iterator();
		  while (it.hasNext()){
			  pw.println(it.next());
		  }
	  }


	  /**
	   * @author Mithun Acharya
	   * Dumb printing of JUnit 4.0 test class
	   * FIXME: getting class name and object name is not smart.
	   */
	  private void printJUnitTestClass(PrintWriter pw){
		  // imports
		  pw.println("import static org.junit.Assert.*;");
		  pw.println("import org.junit.Before;");
		  pw.println("import org.junit.Test;");

		  String objectName = (className.toLowerCase()).replace(".", "_");

		  pw.println();
		  pw.println("public class " + className.replace(".", "_") + "Test {"); // test class
		  pw.println();
		  pw.println("	private " + className + " " + objectName + ";"); // CUT object to be tested
		  pw.println();
		  pw.println("	@Before"); // setUp method annotation
		  pw.println("	public void setUp() throws Exception {"); // setUp method
		  pw.println("		" + objectName + " = new " + className + "();"); // create object for CUT
		  pw.println("	}"); // setUp method end
		  // Create a test method for each sequence
		  int testIndex = 0;
		  Iterator<Vector> it = methodSequences.iterator();
		  while (it.hasNext()){
			  Vector<String> methodSequence = it.next();
			  pw.println();
			  Iterator<String> it1 = methodSequence.iterator();
			  if (it1.hasNext()) {
				  String errAnn = (String)(it1.next());

				  if (errAnn.contains("expected")) {
					  pw.println("	@Test"+errAnn); // Corina: added @Test annotation with exception expected
				  }
				  else {
					  pw.println("	@Test"); // @Test annotation
					  it1 = methodSequence.iterator();
				  }
			  }
			  else
				  it1 = methodSequence.iterator();

			  //pw.println("	@Test"); // @Test annotation
			  pw.println("	public void test" + testIndex + "() {"); // begin test method
			  //Iterator<String> it1 = methodSequence.iterator();
			  while(it1.hasNext()){
				  String invokedMethod = it1.next();
				  if (invokedMethod.contains(exceptionMarker)) { // error-string. not a method
					  // add a comment about the exception
					  pw.println("		" + "//should lead to " + invokedMethod);
				  }
				  else{ // normal method
					  pw.println(invokedMethod); // invoke a method in the sequence
				  }
			  }
			  pw.println("	}"); // end test method
			  testIndex++;
		  }
		  pw.println("}"); // test class end
	  }

}

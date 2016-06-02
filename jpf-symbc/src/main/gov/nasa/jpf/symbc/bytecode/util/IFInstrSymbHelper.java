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

/**
 * 
 */
package gov.nasa.jpf.symbc.bytecode.util;


import gov.nasa.jpf.jvm.bytecode.IfInstruction;
import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.expressions.Constant;
import gov.nasa.jpf.constraints.expressions.NumericBooleanExpression;
import gov.nasa.jpf.constraints.expressions.NumericComparator;
import gov.nasa.jpf.constraints.types.BuiltinTypes;
import gov.nasa.jpf.symbc.bytecode.LCMP;
import gov.nasa.jpf.symbc.jconstraints.JPCChoiceGenerator;
import gov.nasa.jpf.symbc.jconstraints.JPathCondition;
import gov.nasa.jpf.symbc.jconstraints.Translate;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Types;

/**
 * @author Kasper S. Luckow <luckow@cs.aau.dk>
 * 
 * Deals with how symbolic conditions are handled. Currently a lot(!!) of redundancy. Furthermore, parts of it
 * are so ugly that my eyes bleed. Should be refactored into a generic method.
 */
public class IFInstrSymbHelper {
	
	public static Instruction getNextInstructionAndSetPCChoice(ThreadInfo ti, 
															   LCMP instr, 
															   Expression<?> sym_v1_ex,
															   Expression<?> sym_v2_ex,
															   NumericComparator firstComparator,
															   NumericComparator secondComparator,
															   NumericComparator thirdComparator) {
		int conditionValue = -3; //bogus value
	    long v1 = ti.getModifiableTopFrame().peekLong();
		long v2 = ti.getModifiableTopFrame().peekLong(2);
        Expression<Long> sym_v1 = Translate.translateLong(sym_v1_ex, v1);
        Expression<Long> sym_v2 = Translate.translateLong(sym_v2_ex, v2);

		if(!ti.isFirstStepInsn()) { // first time around
			JPCChoiceGenerator prevPcGen;
			ChoiceGenerator<?> cg = ti.getVM().getChoiceGenerator();
			if(cg instanceof JPCChoiceGenerator)
				prevPcGen = (JPCChoiceGenerator)cg;
			else 
				prevPcGen = (JPCChoiceGenerator)cg.getPreviousChoiceGeneratorOfType(JPCChoiceGenerator.class);
		
			JPathCondition pc;
			if(prevPcGen!=null)
				pc = prevPcGen.getCurrentPC();
			else
				pc = new JPathCondition();
			
			JPathCondition firstPC = pc.make_copy();
			JPathCondition secPC = pc.make_copy();
			JPathCondition thirdPC = pc.make_copy();
			
            firstPC._addDet(new NumericBooleanExpression(sym_v2, firstComparator, sym_v1));
            secPC._addDet(new NumericBooleanExpression(sym_v1, secondComparator, sym_v2));
            thirdPC._addDet(new NumericBooleanExpression(sym_v2, thirdComparator, sym_v1));
			
			boolean firstSat = firstPC.simplify();
			boolean secSat = secPC.simplify();
			boolean thirdSat = thirdPC.simplify();
			
			if(firstSat) {
				if(secSat) {
					JPCChoiceGenerator newPCChoice;
					if(thirdSat) {
						newPCChoice = new JPCChoiceGenerator(3);
					} else {
						//LE (choice 0) == true, EQ (choice 1)== true
						newPCChoice = new JPCChoiceGenerator(2);
					}
					newPCChoice.setOffset(instr.getPosition());
					newPCChoice.setMethodName(instr.getMethodInfo().getFullName());
					ti.getVM().getSystemState().setNextChoiceGenerator(newPCChoice);
					return instr;
				} else if(thirdSat) {
					//LE (choice 0) == true, GT (choice 2)== true
					JPCChoiceGenerator newPCChoice = new JPCChoiceGenerator(0, 2, 2);
					newPCChoice.setOffset(instr.getPosition());
					newPCChoice.setMethodName(instr.getMethodInfo().getFullName());
					ti.getVM().getSystemState().setNextChoiceGenerator(newPCChoice);
					return instr;
				} else {
					prevPcGen.setCurrentPC(firstPC);
					conditionValue = -1;
				}
			} else if(secSat) {
				if(thirdSat) {
					//EQ (choice 1) == true, GT (choice 2)== true
					JPCChoiceGenerator newPCChoice = new JPCChoiceGenerator(1, 2);
					newPCChoice.setOffset(instr.getPosition());
					newPCChoice.setMethodName(instr.getMethodInfo().getFullName());
					ti.getVM().getSystemState().setNextChoiceGenerator(newPCChoice);
					return instr;
				} else {
					conditionValue = 0;
				}
			} else if(thirdSat) {
				conditionValue = 1;
			} else {
				System.err.println("***********Warning: everything false");
				ti.getVM().getSystemState().setIgnored(true);
			}
			
		} else { //This branch will only be taken if there is a choice
			
			JPathCondition pc;
			JPCChoiceGenerator curCg = (JPCChoiceGenerator)ti.getVM().getSystemState().getChoiceGenerator();
			
			JPCChoiceGenerator prevCg = curCg.getPreviousChoiceGeneratorOfType(JPCChoiceGenerator.class);
			
			if(prevCg == null )
				pc = new JPathCondition();
			else
				pc = ((JPCChoiceGenerator)prevCg).getCurrentPC();
			
			conditionValue = ((JPCChoiceGenerator) curCg).getNextChoice() -1;
			if (conditionValue == -1) {
                pc._addDet(new NumericBooleanExpression(sym_v2, firstComparator, sym_v1));
				((JPCChoiceGenerator) curCg).setCurrentPC(pc);
			} else if (conditionValue == 0){
                pc._addDet(new NumericBooleanExpression(sym_v1, secondComparator, sym_v2));
				((JPCChoiceGenerator) curCg).setCurrentPC(pc);
			} else {// 1
                pc._addDet(new NumericBooleanExpression(sym_v2, thirdComparator, sym_v1));
				((JPCChoiceGenerator) curCg).setCurrentPC(pc);
			}
		}
		ti.getModifiableTopFrame().popLong();
		ti.getModifiableTopFrame().popLong();
		ti.getModifiableTopFrame().push(conditionValue, false);
		return instr.getNext(ti);
	}
	
	public static Instruction getNextInstructionAndSetPCChoiceFloat(ThreadInfo ti, 
																   Instruction instr, 
																   Expression<?> sym_v1_ex,
																   Expression<?> sym_v2_ex,
																   NumericComparator firstComparator,
																   NumericComparator secondComparator,
																   NumericComparator thirdComparator) {
		int conditionValue = -3; //bogus value
		float v1 = Types.intToFloat(ti.getModifiableTopFrame().peek());
		float v2 = Types.intToFloat(ti.getModifiableTopFrame().peek(1));
        Expression<Float> sym_v1 = Translate.translateFloat(sym_v1_ex, v1);
        Expression<Float> sym_v2 = Translate.translateFloat(sym_v2_ex, v2);

		if(!ti.isFirstStepInsn()) { // first time around
			JPCChoiceGenerator prevPcGen;
			ChoiceGenerator<?> cg = ti.getVM().getChoiceGenerator();
			if(cg instanceof JPCChoiceGenerator)
				prevPcGen = (JPCChoiceGenerator)cg;
			else 
				prevPcGen = (JPCChoiceGenerator)cg.getPreviousChoiceGeneratorOfType(JPCChoiceGenerator.class);
		
			JPathCondition pc;
			if(prevPcGen!=null)
				pc = prevPcGen.getCurrentPC();
			else
				pc = new JPathCondition();
			
			JPathCondition firstPC = pc.make_copy();
			JPathCondition secPC = pc.make_copy();
			JPathCondition thirdPC = pc.make_copy();
			
			firstPC._addDet(new NumericBooleanExpression(sym_v2, firstComparator, sym_v1));
			secPC._addDet(new NumericBooleanExpression(sym_v1, secondComparator, sym_v2));
			thirdPC._addDet(new NumericBooleanExpression(sym_v2, thirdComparator, sym_v1));
			
			boolean firstSat = firstPC.simplify();
			boolean secSat = secPC.simplify();
			boolean thirdSat = thirdPC.simplify(); 
			
			if(firstSat) {
				if(secSat) {
					JPCChoiceGenerator newPCChoice;
					if(thirdSat) {
						newPCChoice = new JPCChoiceGenerator(3);
					} else {
						//LE (choice 0) == true, EQ (choice 1)== true
						newPCChoice = new JPCChoiceGenerator(2);
					}
					newPCChoice.setOffset(instr.getPosition());
					newPCChoice.setMethodName(instr.getMethodInfo().getFullName());
					ti.getVM().getSystemState().setNextChoiceGenerator(newPCChoice);
					return instr;
				} else if(thirdSat) {
					//LE (choice 0) == true, GT (choice 2)== true
					JPCChoiceGenerator newPCChoice = new JPCChoiceGenerator(0, 2, 2);
					newPCChoice.setOffset(instr.getPosition());
					newPCChoice.setMethodName(instr.getMethodInfo().getFullName());
					ti.getVM().getSystemState().setNextChoiceGenerator(newPCChoice);
					return instr;
				} else {
					prevPcGen.setCurrentPC(firstPC);
					conditionValue = -1;
				}
			} else if(secSat) {
				if(thirdSat) {
					//EQ (choice 1) == true, GT (choice 2)== true
					JPCChoiceGenerator newPCChoice = new JPCChoiceGenerator(1, 2);
					newPCChoice.setOffset(instr.getPosition());
					newPCChoice.setMethodName(instr.getMethodInfo().getFullName());
					ti.getVM().getSystemState().setNextChoiceGenerator(newPCChoice);
					return instr;
				} else {
					conditionValue = 0;
				}
			} else if(thirdSat) {
				conditionValue = 1;
			} else {
				System.err.println("***********Warning: everything false");
				ti.getVM().getSystemState().setIgnored(true);
			}
		} else { //This branch will only be taken if there is a choice
			
			JPathCondition pc;
			JPCChoiceGenerator curCg = (JPCChoiceGenerator)ti.getVM().getSystemState().getChoiceGenerator();
			
			JPCChoiceGenerator prevCg = curCg.getPreviousChoiceGeneratorOfType(JPCChoiceGenerator.class);
			
			if(prevCg == null )
				pc = new JPathCondition();
			else
				pc = prevCg.getCurrentPC();
			
			conditionValue = ((JPCChoiceGenerator) curCg).getNextChoice() -1;
			if (conditionValue == -1) {
                pc._addDet(new NumericBooleanExpression(sym_v2, firstComparator, sym_v1));
				((JPCChoiceGenerator) curCg).setCurrentPC(pc);
			} else if (conditionValue == 0){
                pc._addDet(new NumericBooleanExpression(sym_v1, secondComparator, sym_v2));
				((JPCChoiceGenerator) curCg).setCurrentPC(pc);
			} else {// 1
                pc._addDet(new NumericBooleanExpression(sym_v2, thirdComparator, sym_v1));
				((JPCChoiceGenerator) curCg).setCurrentPC(pc);
			}
		}
		ti.getModifiableTopFrame().pop();
		ti.getModifiableTopFrame().pop();
		ti.getModifiableTopFrame().push(conditionValue, false);
		return instr.getNext(ti);
		
	}
	
	public static Instruction getNextInstructionAndSetPCChoiceDouble(ThreadInfo ti, 
															   Instruction instr, 
															   Expression<?> sym_v1_ex,
															   Expression<?> sym_v2_ex,
															   NumericComparator firstComparator,
															   NumericComparator secondComparator,
															   NumericComparator thirdComparator) {
		int conditionValue = -3; //bogus value
		double v1 = Types.longToDouble(ti.getModifiableTopFrame().peekLong());
		double v2 = Types.longToDouble(ti.getModifiableTopFrame().peekLong(2));
		Expression<Double> sym_v1 = Translate.translateDouble(sym_v1_ex, v1);
		Expression<Double> sym_v2 = Translate.translateDouble(sym_v2_ex, v2);

		if(!ti.isFirstStepInsn()) { // first time around
			JPCChoiceGenerator prevPcGen;
			ChoiceGenerator<?> cg = ti.getVM().getChoiceGenerator();
			if(cg instanceof JPCChoiceGenerator)
				prevPcGen = (JPCChoiceGenerator)cg;
			else 
				prevPcGen = (JPCChoiceGenerator)cg.getPreviousChoiceGeneratorOfType(JPCChoiceGenerator.class);
		
			JPathCondition pc;
			if(prevPcGen!=null)
				pc = prevPcGen.getCurrentPC();
			else
				pc = new JPathCondition();
			
			JPathCondition firstPC = pc.make_copy();
			JPathCondition secPC = pc.make_copy();
			JPathCondition thirdPC = pc.make_copy();
			
            firstPC._addDet(new NumericBooleanExpression(sym_v2, firstComparator, sym_v1));
            secPC._addDet(new NumericBooleanExpression(sym_v1, secondComparator, sym_v2));
            thirdPC._addDet(new NumericBooleanExpression(sym_v2, thirdComparator, sym_v1));
			
			boolean firstSat = firstPC.simplify();
			boolean secSat = secPC.simplify();
			boolean thirdSat = thirdPC.simplify();
			
			if(firstSat) {
				if(secSat) {
					JPCChoiceGenerator newPCChoice;
					if(thirdSat) {
						newPCChoice = new JPCChoiceGenerator(3);
					} else {
						//LE (choice 0) == true, EQ (choice 1)== true
						newPCChoice = new JPCChoiceGenerator(2);
					}
					newPCChoice.setOffset(instr.getPosition());
					newPCChoice.setMethodName(instr.getMethodInfo().getFullName());
					ti.getVM().getSystemState().setNextChoiceGenerator(newPCChoice);
					return instr;
				} else if(thirdSat) {
					//LE (choice 0) == true, GT (choice 2)== true
					JPCChoiceGenerator newPCChoice = new JPCChoiceGenerator(0, 2, 2);
					newPCChoice.setOffset(instr.getPosition());
					newPCChoice.setMethodName(instr.getMethodInfo().getFullName());
					ti.getVM().getSystemState().setNextChoiceGenerator(newPCChoice);
					return instr;
				} else {
					conditionValue = -1;
				}
			} else if(secSat) {
				if(thirdSat) {
					//EQ (choice 1) == true, GT (choice 2)== true
					JPCChoiceGenerator newPCChoice = new JPCChoiceGenerator(1, 2);
					newPCChoice.setOffset(instr.getPosition());
					newPCChoice.setMethodName(instr.getMethodInfo().getFullName());
					ti.getVM().getSystemState().setNextChoiceGenerator(newPCChoice);
					return instr;
				} else {
					conditionValue = 0;
				}
			} else if(thirdSat) {
				conditionValue = 1;
			} else {
				System.err.println("***********Warning: everything false");
				ti.getVM().getSystemState().setIgnored(true);
			}
		} else { //This branch will only be taken if there is a choice
			
			JPathCondition pc;
			JPCChoiceGenerator curCg = (JPCChoiceGenerator)ti.getVM().getSystemState().getChoiceGenerator();
			
			JPCChoiceGenerator prevCg = curCg.getPreviousChoiceGeneratorOfType(JPCChoiceGenerator.class);
			
			if(prevCg == null )
				pc = new JPathCondition();
			else
				pc = prevCg.getCurrentPC();
			
			conditionValue = ((JPCChoiceGenerator) curCg).getNextChoice() -1;
			if (conditionValue == -1) {
                pc._addDet(new NumericBooleanExpression(sym_v2, firstComparator, sym_v1));
				((JPCChoiceGenerator) curCg).setCurrentPC(pc);
			} else if (conditionValue == 0){
                pc._addDet(new NumericBooleanExpression(sym_v1, secondComparator, sym_v2));
				((JPCChoiceGenerator) curCg).setCurrentPC(pc);
			} else {// 1
                pc._addDet(new NumericBooleanExpression(sym_v2, thirdComparator, sym_v1));
				((JPCChoiceGenerator) curCg).setCurrentPC(pc);
			}
		}
		ti.getModifiableTopFrame().popLong();
		ti.getModifiableTopFrame().popLong();
		ti.getModifiableTopFrame().push(conditionValue, false);
		return instr.getNext(ti);
	}
	
	
	
	public static Instruction getNextInstructionAndSetPCChoice(ThreadInfo ti, 
															   IfInstruction instr, 
															   Expression<?> sym_ex,
															   NumericComparator trueComparator,
															   NumericComparator falseComparator) {
		
        Expression<Integer> sym_v = Translate.translateInt(sym_ex); 
        Constant<Integer> zero = Constant.create(BuiltinTypes.SINT32, 0);
		//TODO: fix conditionValue
		if(!ti.isFirstStepInsn()) { // first time around
			JPCChoiceGenerator prevPcGen;
			ChoiceGenerator<?> cg = ti.getVM().getChoiceGenerator();
			if(cg instanceof JPCChoiceGenerator)
				prevPcGen = (JPCChoiceGenerator)cg;
			else 
				prevPcGen = (JPCChoiceGenerator)cg.getPreviousChoiceGeneratorOfType(JPCChoiceGenerator.class);
		
			JPathCondition pc;
			if(prevPcGen!=null)
				pc = prevPcGen.getCurrentPC();
			else
				pc = new JPathCondition();
			
			JPathCondition eqPC = pc.make_copy();
			JPathCondition nePC = pc.make_copy();
			eqPC._addDet(new NumericBooleanExpression(sym_v, trueComparator, zero));
			nePC._addDet(new NumericBooleanExpression(sym_v, falseComparator, zero));
			
			boolean eqSat = eqPC.simplify();
			boolean neSat = nePC.simplify();
			
			if(eqSat) {
				if(neSat) {
					JPCChoiceGenerator newPCChoice;
					newPCChoice = new JPCChoiceGenerator(2);
					newPCChoice.setOffset(instr.getPosition());
					newPCChoice.setMethodName(instr.getMethodInfo().getFullName());
					ti.getVM().getSystemState().setNextChoiceGenerator(newPCChoice);
					return instr;
				} else {
					ti.getModifiableTopFrame().pop();
					return instr.getTarget();
				}
			} else {
				ti.getModifiableTopFrame().pop();
				return instr.getNext(ti);
			}	
		} else {
			ti.getModifiableTopFrame().pop();
			JPathCondition pc;
			JPCChoiceGenerator curCg = (JPCChoiceGenerator)ti.getVM().getSystemState().getChoiceGenerator();
			
			JPCChoiceGenerator prevCg = curCg.getPreviousChoiceGeneratorOfType(JPCChoiceGenerator.class);
			
			if(prevCg == null )
				pc = new JPathCondition();
			else
				pc = prevCg.getCurrentPC();
			boolean conditionValue = (Integer)curCg.getNextChoice()==1 ? true: false;
			if(conditionValue) {
				pc._addDet(new NumericBooleanExpression(sym_v, trueComparator, zero));
				((JPCChoiceGenerator) curCg).setCurrentPC(pc);
				return instr.getTarget();
			} else {
				pc._addDet(new NumericBooleanExpression(sym_v, falseComparator, zero));
				((JPCChoiceGenerator) curCg).setCurrentPC(pc);
				return instr.getNext(ti);
			}
		}	
	}
	
	public static Instruction getNextInstructionAndSetPCChoice(ThreadInfo ti, 
															   IfInstruction instr, 
															   Expression<?> sym_v1_ex, 
															   Expression<?> sym_v2_ex,
															   NumericComparator trueComparator,
															   NumericComparator falseComparator) {
		

	    int	v2 = ti.getModifiableTopFrame().peek();
		int	v1 = ti.getModifiableTopFrame().peek(1);
        Expression<Integer> sym_v1 = Translate.translateInt(sym_v1_ex, v1);
        Expression<Integer> sym_v2 = Translate.translateInt(sym_v2_ex, v2);


		//TODO: fix conditionValue
		if(!ti.isFirstStepInsn()) { // first time around
			JPCChoiceGenerator prevPcGen;
			ChoiceGenerator<?> cg = ti.getVM().getChoiceGenerator();
			if(cg instanceof JPCChoiceGenerator)
				prevPcGen = (JPCChoiceGenerator)cg;
			else 
				prevPcGen = (JPCChoiceGenerator)cg.getPreviousChoiceGeneratorOfType(JPCChoiceGenerator.class);
		
			JPathCondition pc;
			if(prevPcGen!=null)
				pc = prevPcGen.getCurrentPC();
			else
				pc = new JPathCondition();
			
			JPathCondition eqPC = pc.make_copy();
			JPathCondition nePC = pc.make_copy();
			
			eqPC._addDet(new NumericBooleanExpression(sym_v1, trueComparator, sym_v2));
			nePC._addDet(new NumericBooleanExpression(sym_v1, falseComparator, sym_v2));

			boolean eqSat = eqPC.simplify();
			boolean neSat = nePC.simplify();
			
			if(eqSat) {
				if(neSat) {
					JPCChoiceGenerator newPCChoice = new JPCChoiceGenerator(2);
					newPCChoice.setOffset(instr.getPosition());
					newPCChoice.setMethodName(instr.getMethodInfo().getFullName());
					ti.getVM().getSystemState().setNextChoiceGenerator(newPCChoice);
					return instr;
				} else {
					ti.getModifiableTopFrame().pop();
					ti.getModifiableTopFrame().pop();
					return instr.getTarget();
				}
			} else {
				ti.getModifiableTopFrame().pop();
				ti.getModifiableTopFrame().pop();
				return instr.getNext(ti);
			}	
		} else { //This branch will only be taken if there is a choice
			
			ti.getModifiableTopFrame().pop();
			ti.getModifiableTopFrame().pop();
			JPathCondition pc;
			JPCChoiceGenerator curCg = (JPCChoiceGenerator)ti.getVM().getSystemState().getChoiceGenerator();
			
			JPCChoiceGenerator prevCg = curCg.getPreviousChoiceGeneratorOfType(JPCChoiceGenerator.class);
			
			if(prevCg == null )
				pc = new JPathCondition();
			else
				pc = prevCg.getCurrentPC();
			
			boolean conditionValue = (Integer)curCg.getNextChoice()==1 ? true: false;
			if(conditionValue) {
                pc._addDet(new NumericBooleanExpression(sym_v1, trueComparator, sym_v2));
				((JPCChoiceGenerator) curCg).setCurrentPC(pc);
				return instr.getTarget();
			} else {
                pc._addDet(new NumericBooleanExpression(sym_v1, falseComparator, sym_v2));
				((JPCChoiceGenerator) curCg).setCurrentPC(pc);
				return instr.getNext(ti);
			}
		}		
	}
}

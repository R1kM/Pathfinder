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
//Copyright (C) 2006 United States Government as represented by the
//Administrator of the National Aeronautics and Space Administration
//(NASA).  All Rights Reserved.
//
//This software is distributed under the NASA Open Source Agreement
//(NOSA), version 1.3.  The NOSA has been approved by the Open Source
//Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
//directory tree for the complete NOSA document.
//
//THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
//KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
//LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
//SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
//A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
//THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
//DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
//

package gov.nasa.jpf.symbc.jconstraints;

//import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Instance;
import gov.nasa.jpf.constraints.api.ConstraintSolver;
import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.api.Valuation;
import gov.nasa.jpf.constraints.solvers.ConstraintSolverFactory;
import gov.nasa.jpf.symbc.SymbolicInstructionFactory;
import gov.nasa.jpf.symbc.concolic.PCAnalyzer;
import gov.nasa.jpf.symbc.concolic.*;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.VM;

// path condition contains mixed constraints of integers and reals

public class JPathCondition {
    public static boolean flagSolved = false;

    public Jconstraint header;
    int count = 0;

    private Integer hashCode = null;

    //added by guowei
    public static boolean isReplay = false;
    public static void setReplay(boolean isReplay){
		JPathCondition.isReplay = isReplay;
	}

    public JPathCondition() {
    	header = null;
    }

	public JPathCondition make_copy() {
		JPathCondition pc_new = new JPathCondition();
		pc_new.header = this.header;
	    pc_new.count = this.count;
		return pc_new;
	}

	public void _addDet (Expression<Boolean> loic) {
		//throw new RuntimeException ("Not being used right now");
	    Jconstraint t = new Jconstraint(loic);
		if (!this.hasJconstraint(t)) {
			flagSolved = false;
			t.and = header;
			header = t;
			count++;
		}
	}

    public boolean prependUnlessRepeated(Jconstraint c) {
       c.and = header;
       header = c;
       count ++;
       return true;
    }

    public boolean solve() {
        Expression<Boolean> res = header.conjunctionConstraints();
        if (SymbolicInstructionFactory.dp == null) 
            throw new RuntimeException("Please specify a solver to use in config");
        ConstraintSolverFactory cFactory = new ConstraintSolverFactory();
        ConstraintSolver solver = cFactory.createSolver(SymbolicInstructionFactory.dp[0]);
        switch(solver.solve(res, new Valuation())) {
            case SAT:
            case DONT_KNOW:
                return true;
            case UNSAT:
                return false;
        }
        return false;
    }
        

    public boolean simplify() {
        Expression<Boolean> res = header.conjunctionConstraints();
        if (SymbolicInstructionFactory.dp == null)
            throw new RuntimeException("Please specify a solver to use in config");
        ConstraintSolverFactory cFactory = new ConstraintSolverFactory();
        ConstraintSolver solver = cFactory.createSolver(SymbolicInstructionFactory.dp[0]);
        switch(solver.isSatisfiable(res)) {
            case SAT:
            case DONT_KNOW:
                return true;
            case UNSAT:
                return false;
        }
        return false;
    }


    public void prependAllConjuncts(Jconstraint t) {
       t.last().and = header;
       header = t;
       count= length(header);
    }

    public void appendAllConjuncts(Jconstraint t) {
        Jconstraint tmp = header.last();
        tmp.and = t;
        count= length(header);
     }

    private static int length(Jconstraint c) {
        int x= 0;
        while (c != null) {
            x++;
            c = c.getTail();
        }
        return x;
    }

    /**
     * Returns the number of constraints in this path condition.
     */
	public int count() {
		return count;
	}

	/**
	 * Returns whether this path condition contains the constraint.
	 */
	public boolean hasJconstraint(Jconstraint c) {
		Jconstraint t = header;

		while (t != null) {
			if (c.equals(t)) {
				return true;
			}

			t = t.and;
		}

		return false;
	}

	public Jconstraint last() {
		Jconstraint t = header;
		Jconstraint last = null;
		while (t != null) {
			last = t;
			t = t.and;
		}

		return last;
	}

		/*
		 * This is untested and have shown a few issues so needs fixing first
		if (isSat) {
			for (Variable v : instance.getSlicedVariables()) {
				Object o = v.getOriginal();
				if (o instanceof SymbolicReal) {
					SymbolicReal r = (SymbolicReal) o;
					r.solution = instance.getRealValue((RealVariable) v);
					//System.out.println("r = " + r.solution);
				} else if (o instanceof SymbolicInteger) {
					SymbolicInteger r = (SymbolicInteger) o;
					r.solution = instance.getIntValue((IntVariable) v);
					//System.out.println("r = " + r.solution);
				}
			}
		}
		*/

	public String stringPC() {
		return "constraint # = " + count + ((header == null) ? "" : "\n" + header.stringPC());
	}

	public String toString() {
		return "constraint # = " + count + ((header == null) ? "" : "\n" + header.toString());
		//return ((header == null) ? "" : " " + header.toString()); -- for specialization
					//+ "\n" + spc.toString(); // TODO: to review
	}

	public static JPathCondition getPC(MJIEnv env) {
	   VM vm = env.getVM();
	   return getPC(vm);
	}

	public static JPathCondition getPC(VM vm) {
	    ChoiceGenerator<?> cg = vm.getChoiceGenerator();
	    if (cg != null && !(cg instanceof JPCChoiceGenerator)) {
	        cg = cg.getPreviousChoiceGeneratorOfType(JPCChoiceGenerator.class);
	    }

	    if (cg instanceof JPCChoiceGenerator) {
	        return ((JPCChoiceGenerator) cg).getCurrentPC();
	    } else {
	        return null;
	    }
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 * 
	 * Note: Technically, this routine is incomplete and should take the string
	 * path condition stored in field {@code spc} into account.
	 * 
	 * @param obj
	 *            the reference object with which to compare
	 * @return {@code true} if this object is the same as the obj argument;
	 *         {@code false} otherwise.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		JPathCondition p = (JPathCondition) obj;
		if (count != p.count) {
			return false;
		}
		Jconstraint c = header;
		Jconstraint pc = p.header;
		while (c != null) {
			if (pc == null) {
				return false;
			}
			if (!c.equals(pc)) {
				return false;
			}
			c = c.getTail();
			pc = pc.getTail();
		}
		if (pc != null) {
			return false;
		}
		return true;
	}


	/**
	 * Returns a hash code value for the object.
	 * 
	 * Note: Technically, this routine is incomplete and should take the string
	 * path condition stored in field {@code spc} into account.
	 * 
	 * @return a hash code value for this object
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (hashCode == null) {
			hashCode = new Integer(0);
			Jconstraint c = header;
			while (c != null) {
				hashCode = hashCode ^ c.hashCode();
				c = c.getTail();
			}
		}
		return hashCode;
	}

	/**
	 * Sometimes we violate our abstraction and fiddle with the fields of a path
	 * condition. Whenever the list of constraints rooted in {@link #header} is
	 * modified in any way, this routine should be called to force the
	 * re-computation of the hash value of the path condition.
	 */
	public void resetHashCode() {
		hashCode = null;
	}

	/**
	 * Recompute the value of {@link #count}, based on the actual list of
	 * constraints.
	 */
	public void recomputeCount() {
		count = 0;
		for (Jconstraint c = header; c != null; c = c.getTail()) {
			count++;
		}
	}

	/**
	 * Remove the header of the path condition, update the count, and reset the
	 * hash code.
	 */
	public void removeHeader() {
		assert header != null;
		header = header.and;
		count--;
		resetHashCode();
	}


}

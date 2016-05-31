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

package gov.nasa.jpf.symbc.numeric;

import gov.nasa.jpf.constraints.api.Expression;

import java.util.Map;

public abstract class Jconstraint {
  private final Expression<Boolean> head;

  public Jconstraint and;

  public Jconstraint(Expression<Boolean> h) {
      head = h;
  }

  /** Returns the head expression. Subclasses may override to give tighter type bounds.*/
  public Expression getHead() {
      return head;
  }

  /**
   * Returns the negation of this constraint, but without the tail.
   */
  public abstract Jconstraint not();

  /**
   * Returns the next conjunct.
   */
  public Jconstraint getTail() {
    return and;
  }

  public String stringPC() {
    return head.toString(Expression.DEFAULT_FLAGS)
        + ((and == null) ? "" : " && " + and.stringPC());
  }

  public boolean equals(Object o) {
    if (!(o instanceof Jconstraint)) {
      return false;
    }

    return head.equals(((Jconstraint) o).head);
  }

  public int hashCode() {
	  int result = Integer.MAX_VALUE;
	  if (head != null) {
		  result = result ^ head.hashCode();
	  }
	  return result;
	  //return left.hashCode() ^ comp.hashCode() ^ right.hashCode();
  }

  public String toString() {
    return head.toString(Expression.DEFAULT_FLAGS)
        //+ ((and == null) ? "" : " && " + and.toString()); -- for specialization
        + ((and == null) ? "" : " &&\n" + and.toString());
  }

  public Jconstraint last() {
      Jconstraint c= this;
      while(c.and != null) {
          c = c.and;
      }
      return c;
  }
  
}

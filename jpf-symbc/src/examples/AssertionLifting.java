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

import gov.nasa.jpf.symbc.Debug;



public class AssertionLifting {
	public static void test(int x) {
		System.out.println("br1");
		// new assertion should be x>0 && x<=5
		Debug.freshPCcopy();
		Debug.addGT0(x);
		Debug.addGT0(6-x);
		boolean result = Debug.checkSAT();
		
		//compare with result = x>0 && x<=5
		System.out.println("result "+result+" "+Debug.getSolvedPC());
		
		//if(x>0&&x<=5)
		//	assert false;
		if(x>0) {
			//assert(x>5);
			if(x<=5) {
				System.out.println("assert violated "+Debug.getSolvedPC());
			}
				
			System.out.println("br2");
		}
		else
			System.out.println("br3");
	}
	
	// The test driver
	public static void main(String[] args) {
		test(0);
	}
}
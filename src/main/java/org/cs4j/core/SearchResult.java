/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cs4j.core;

import java.util.List;

import org.cs4j.core.SearchDomain.Operator;

/**
 * The search result interface.
 * 
 * @author Matthew Hatem
 */
public interface SearchResult {
	
  /**
   * Returns the solution path.
   * 
   * @return the solution path
   */
  public List<Solution> getSolutions();
		  
  /**
   * Returns expanded count.
   * 
   * @return expanded count
   */
  public double getExpanded();
	  
  /**
   * Returns generated count.
   * 
   * @return generated count
   */
	public double getGenerated();
	  
	/**
	 * Returns the wall time in milliseconds.
	 * 
	 * @return the wall time in milliseconds
	 */
	public long getWallTimeMillis();
	  
	/**
	 * Returns the CPU time in milliseconds.
	 * 
	 * @return the CPU time in milliseconds
	 */
	public long getCpuTimeMillis();
	
	/**
	 * Interface for search iterations.
	 */
	public interface Iteration {
		
		/**
		 * Returns the bound for this iteration.
		 * 
		 * @return the bound
		 */
		public double getBound();
		
		/**
		 * Returns the number of nodes expanded.
		 * 
		 * @return the number of nodes expanded
		 */
		public long getExpanded();
		
		/**
		 * Returns the number of nodes generated.
		 * 
		 * @return the number of nodes generated
		 */
		public long getGenerated();
		
	}
	
	/**
	 * The Solution interface.
	 */
	public interface Solution {

		/**
		 * Returns a list of operators used to construct this solution.
		 * 
		 * @return list of operators
		 */
		public List<Operator> getOperators();
		
		/**
		 * Returns the cost of the solution.
		 * 
		 * @return the cost of the solution
		 */
		public double getCost();
		
		/**
		 * Returns the length of the solution.
		 */
		public int getLength();
		
	}
	  	  
}

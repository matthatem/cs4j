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

/**
 * The search domain interface.
 *
 * @param <T> state type
 * @param <O> operator type
 */
public interface SearchDomain {
  
	/**
	 * Returns the initial state for an instance of Domain.
	 * 
	 *  @return the initial state
	 */
	public State initialState();
	      
  /**
   * Returns true if the specified state is the goal state, false otherwise.
   * 
   * @param state the state
   * @return true if s is a goal state, false otherwise
   */
  public boolean isGoal(State state);
 
  /**
   * Returns the number of operators applicable for the specified state.
   * 
   * @param state the state
   * @return the number of operators
   */
  public int getNumOperators(State state);
  
  /**
   * Returns the specified operator applicable for the specified state.
   * 
   * @param state the state
   * @param nth the nth operator index
   * @return the nth operator
   */
  public Operator getOperator(State state, int index);
  
  /**
   * Applies the specified operator to the specified state and returns an
   * a new edge.  
   * 
   * @param state the state
   * @param op the operator
   * @return the new edge
   */
  public State applyOperator(State state, Operator op);
  
  /**
   * Returns a copy of the specified state.
   * 
   * @param state the state
   * @return the copy
   */
  public State copy(State state);

  /**
   * Packs a representation of the specified state into a long.
   * 
   * @param state the state
   * @return the packed state as a long
   */
  public long pack(State state);
  
  /**
   * Unpacks the specified packed representation into a new state.
   * 
   * @param packed the long representation
   * @return the new state
   */
  public State unpack(long packed);  
  
  /**
   * The State interface.
   */
  public interface State {
  	
  	/**
  	 * Returns the heuristic estimate for the state.
  	 * 
  	 * @return the heuristic estimate
  	 */
  	public double getH();
  	
  	/**
  	 * Returns the distance estimate for the state.
  	 * 
  	 * @return the distance estimate
  	 */
  	public double getD(); 
  
  }
  
  /**
   * The Operator interface.
   */
  public interface Operator {
  	
  	/**
  	 * Returns the cost for this operator as it applies to the specified state.
  	 * 
  	 * @return the cost
  	 */
  	public double getCost(State state);
  	
  	/**
  	 * Returns the operator that would reverse the operation.
  	 * 
  	 * @param state the state
  	 * @return the reverse operator
  	 */
  	public Operator reverse(State state);
  	
  }
  
}

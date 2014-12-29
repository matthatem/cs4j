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
package org.cs4j.core.algorithms;

import org.cs4j.core.SearchDomain;
import org.cs4j.core.SearchDomain.Operator;
import org.cs4j.core.SearchDomain.State;
import org.cs4j.core.SearchAlgorithm;
import org.cs4j.core.SearchResult;
import org.cs4j.core.algorithms.SearchResultImpl.SolutionImpl;

/**
 * The IDA* class.
 *
 * @author Matthew Hatem
 */
public final class IDAstar implements SearchAlgorithm {
  
	private SolutionImpl solution;
  private double weight = 1.0;
  private double bound;
  private double minoob;
  private long expanded;
  private long generated;

  public IDAstar() {
  	solution = new SolutionImpl();
  	weight = 1.0;
  }
  
  @Override
  public SearchResult search(SearchDomain domain) {
  	SearchResultImpl result = new SearchResultImpl();
  	State root = domain.initialState();
  	result.startTimer();
    bound = weight*root.getH();
    int i = 0;
    do {
      minoob = -1;
      boolean goal = dfs(domain, root, 0, null);
      i++;
      result.addIteration(i, bound, expanded, generated);
      bound = minoob;
      if (goal) break;
    } while (true);
    result.stopTimer();
    result.setGenerated(generated);
    result.setExpanded(expanded);
    result.addSolution(solution);
    return result;
  }

  boolean dfs(SearchDomain domain, State parent, double cost, Operator pop) {
    double f = cost + weight*parent.getH();
    
    if (f <= bound && domain.isGoal(parent)) {
      solution.setCost(f);
      solution.addOperator(pop);
      return true;
    }

    if (f > bound) {
      if (minoob < 0 || f < minoob)
        minoob = f;
      return false;
    }

    expanded++;
    int numOps = domain.getNumOperators(parent);
    for (int i=0; i<numOps; i++) {
    	Operator op = domain.getOperator(parent, i);
      if (op.equals(pop))
        continue;

      generated++;
      State child = domain.applyOperator(parent, op);
      boolean goal = dfs(domain, child, op.getCost(parent)+cost, op.reverse(parent));
      if (goal) {
        solution.addOperator(op);
        return true;
      }
    }
    
    return false;
  }
  
}

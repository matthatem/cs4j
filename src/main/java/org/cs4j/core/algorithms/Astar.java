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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cs4j.core.SearchAlgorithm;
import org.cs4j.core.SearchDomain;
import org.cs4j.core.SearchDomain.Operator;
import org.cs4j.core.SearchDomain.State;
import org.cs4j.core.SearchResult;
import org.cs4j.core.algorithms.SearchResultImpl.SolutionImpl;
import org.cs4j.core.collections.BinHeap;
import org.cs4j.core.collections.Indexable;

/**
 * The A* class.  This class implements the A* algorithm using a 
 * binary heap priority queue.
 * 
 * @author Matthew Hatem
 */
public final class Astar implements SearchAlgorithm {
    
  private List<Operator> path = new ArrayList<Operator>(3);
  private SearchDomain domain;
  private long expanded;
  private long generated;
  private double weight = 1;
  
  private Map<Long, Node> closed = new HashMap<>();
  
  private BinHeap<Node> open = 
      new BinHeap<Node>(new NodeComparator());
  
  /**
   * The Constructor
   */
  public Astar() {
  	this(1.0);
  }
  
  /**
   * The Constructor
   * 
   * @param weight the weight for Weighted A*
   */
  public Astar(double weight) {
  	this.weight = weight;
  }
  
  @Override
  public SearchResult search(SearchDomain domain) {
  	this.domain = domain;
  	double goalCost = Double.MAX_VALUE;
  	State state = domain.initialState();
    
  	SearchResultImpl result = new SearchResultImpl();
  	result.startTimer();
    Node initNode = new Node (state);    
    open.add(initNode);
    closed.put(initNode.packed, initNode);
    while (!open.isEmpty() && path.isEmpty()) {
      Node n = open.poll();
      state = domain.unpack(n.packed);
      
      // check for goal
      if (domain.isGoal(state)) {
      	goalCost = n.g;
        for (Node p = n; p != null; p = p.parent) {
            path.add(p.op);
        }
        break;
      }
      
      // expand the node
      expanded++;
      for (int i = 0; i < domain.getNumOperators(state); i++) {
          Operator op = domain.getOperator(state, i);
          if (op.equals(n.pop)) {
              continue;
          }
          generated++;
          State childState = domain.applyOperator(state, op);          
          Node node = new Node(childState, n, op, op.reverse(state));
          
          // merge duplicates
          if (closed.containsKey(node.packed)) {
            Node dup = closed.get(node.packed);
            if (dup.g > node.g) {
              if (dup.getIndex() != -1) {
                dup.f = node.f;
                dup.g = node.g;
                dup.parent = node.parent;
                open.update(dup.getIndex());
              }
              open.add(node);
              closed.put(node.packed, node);
            }
          }
          else {
            open.add(node);
            closed.put(node.packed, node);
          }
      }
    }
   
    // generate result
    result.stopTimer();
    result.setExpanded(expanded);
    result.setGenerated(generated);
    if (path != null && path.size() > 0) {
    	SolutionImpl solution = new SolutionImpl();
    	solution.addOperators(path);
    	solution.setCost(goalCost);
    	result.addSolution(solution);
    }    
    
    return result;
  }
  
  /*
   * The node class
   */
  private final class Node implements Indexable {
    double f, g;
    Operator op, pop;
    Node parent;
    long packed;
    int heapIndex = -1;
    
    private Node (State state) {
    	this(state, null, 0);
    }
    
    private Node (State state, Node parent, Operator op, Operator pop) {
      this(state, parent, op.getCost(state));
    	this.pop = pop;
      this.op = op;
    }
    
    private Node (State state, Node parent, double cost) {
      this.g = (parent != null) ? parent.g+cost : cost;
      this.f = g + (weight*state.getH());      
      this.parent = parent;
      packed = domain.pack(state);
    }
    
    @Override
    public int getIndex() {
      return heapIndex;
    }
    
    @Override
    public void setIndex(int index) {
      this.heapIndex = index;
    }
  }
  
  /*
   * The node comparator class
   */
  private final class NodeComparator implements Comparator<Node> {
  	@Override
    public int compare(final Node a, final Node b) {
      if (a.f < b.f) return -1;
      if (a.f > b.f) return 1;
      if (a.g > b.g) return -1;
      if (a.g < b.g) return 1;
      return 0;
    }    
  }
  
}

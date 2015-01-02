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
import java.util.Collections;
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
import org.cs4j.core.collections.BucketHeap;
import org.cs4j.core.collections.Heap;
import org.cs4j.core.collections.Heapable;

/**
 * A* Search and Weighted A* Search
 * 
 * @author Matthew Hatem
 */
public final class Astar implements SearchAlgorithm {
    
  private SearchDomain domain;
  private Heap<Node> open;
  private double weight;  
  
  private List<Operator> path = new ArrayList<Operator>(3);
  private Map<Long, Node> closed = new HashMap<>();
  
  public enum HeapType {BIN, BUCKET};
  
  /**
   * The Constructor
   */
  public Astar() {
  	this(1.0, HeapType.BIN);
  }
  
  /**
   * The Constructor
   * 
   * @param weight the weight for Weighted A*
   */
  public Astar(double weight) {
  	this(weight, HeapType.BIN);
  }
  
  /**
   * The Constructor
   * 
   * @param weight the weight for Weighted A*
   * @param heapType the type of heap to use (BIN | BUCKET)
   */
  public Astar(double weight, HeapType heapType) {
  	this.weight = weight;
  	this.open = buildHeap(heapType, 100);
  }
  
  private Heap<Node> buildHeap(HeapType heapType, int size) {
  	Heap<Node> heap = null;
  	switch(heapType) {
  	case BUCKET:
  		heap = new BucketHeap<>(size);
  		break;  	
  	case BIN:
  		heap = new BinHeap<>(new NodeComparator());
  		break;
  	}
  	return heap;
  }
  
  @Override
  public SearchResult search(SearchDomain domain) {
  	this.domain = domain;
  	double goalCost = Double.MAX_VALUE;
    
  	SearchResultImpl result = new SearchResultImpl();
  	result.startTimer();
  	
  	State state = domain.initialState();
    Node initNode = new Node(state);    
    open.add(initNode);
    closed.put(initNode.packed, initNode);
    while (!open.isEmpty()) {
      Node n = open.poll();
      state = domain.unpack(n.packed);
      
      // check for goal
      if (domain.isGoal(state)) {
      	goalCost = n.g;
        for (Node p = n; p != null; p = p.parent) {
            path.add(p.op);
        }
        Collections.reverse(path);
        break;
      }
            
      // expand the node
      result.expanded++;
      for (int i = 0; i < domain.getNumOperators(state); i++) {
          Operator op = domain.getOperator(state, i);
          if (op.equals(n.pop)) {
              continue;
          }
          result.generated++;
          State childState = domain.applyOperator(state, op);
          Node node = new Node(childState, n, op, op.reverse(state));
          
          // merge duplicates
          if (closed.containsKey(node.packed)) {
          	result.duplicates++;
            Node dup = closed.get(node.packed);
            if (dup.g > node.g) {
              dup.f = node.f;
              dup.g = node.g;
              dup.op = node.op;
              dup.pop = node.pop;
              dup.parent = node.parent;
              if (dup.heapIndex[0] != -1) {
                open.update(dup);
              }
              else {
              	result.reopened++;
              	open.add(dup);
              }
            }
          }
          else {
            open.add(node);
            closed.put(node.packed, node);
          }
      }
    }
   
    result.stopTimer();

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
  private final class Node implements Heapable {
    double f, g;
    Operator op, pop;
    Node parent;
    long packed;
    int[] heapIndex = new int[]{-1, -1};
    
    private Node(State state) {
    	this(state, null, null, null);
    }
    
    private Node(State state, Node parent, Operator op, Operator pop) {
    	double cost = (op != null) ? op.getCost(state) : 0;
      this.g = (parent != null) ? parent.g+cost : cost;
      this.f = g + (weight*state.getH());
      this.parent = parent;
      packed = domain.pack(state);
    	this.pop = pop;
      this.op = op;
    }
    
    @Override
    public int getIndex(int level) {
      return heapIndex[level];
    }
    
    @Override
    public void setIndex(int level, int index) {
      this.heapIndex[level] = index;
    }
    
    @Override
    public double getRank(int level) {
    	return (level == 0) ? f : g;
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

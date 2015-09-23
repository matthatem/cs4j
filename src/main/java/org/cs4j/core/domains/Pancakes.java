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
package org.cs4j.core.domains;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.cs4j.core.SearchDomain;

public class Pancakes implements SearchDomain {
  
  private COST_FUNCTION costFunction;
  
  public enum COST_FUNCTION {UNIT, HEAVY};
  
  private int numCakes = 0;
  private int init[];
  private Operator[] oplookup;
  
  public Pancakes(InputStream stream, COST_FUNCTION costFunction) {
    this.costFunction = costFunction;
    try {
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(stream));
      String line = reader.readLine();
      numCakes = Integer.parseInt(line);
      init = new int[numCakes];
      line = reader.readLine();
      String[] cakes = line.split(" ");
      for (int i=0; i<cakes.length; i++) {
        init[i] = Integer.parseInt(cakes[i]);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    for (int i=0; i<numCakes; i++) {
    	oplookup[i] = new PancakeOperator(i+1);
    }
  }
  
  /*
   * Pancake class.
   */
  public static final class PancakeState implements SearchDomain.State {    
    int numCakes, depth;
    int[] cakes;
    double h, d;
        
    PancakeState(PancakeState pancake) {
      this.numCakes = pancake.numCakes;
      this.cakes = new int[numCakes];
      this.h = pancake.h;
      this.d = pancake.d;
      System.arraycopy(pancake.cakes, 0, cakes, 0, pancake.cakes.length);
    }
    
    PancakeState(int numCakes) {
      this.numCakes = numCakes;
      cakes = new int[numCakes];
    }
    
    @Override
    public boolean equals(Object object) {
      PancakeState pancake = (PancakeState)object;
      for (int i = 0; i < numCakes; i++) {
        if (cakes[i] != pancake.cakes[i])
          return false;
      }
      return true;
    }
    
    private void flip(int op) {
      assert (op > 0);
      assert (op < numCakes);
      for (int n = 0; n <= op / 2; n++) {
        int tmp = cakes[n];
        cakes[n] = cakes[op - n];
        cakes[op - n] = tmp;
      }
    }

    @Override
    public double getH() {
      return h;
    }

    @Override
    public double getD() {
      return d;
    }
    
  }

  @Override
  public PancakeState initialState() {
    PancakeState s = new PancakeState(numCakes);
    for (int i = 0; i < numCakes; i++)
      s.cakes[i] = init[i];
    s.h = ngaps(s.cakes, costFunction);
    s.d = ngaps(s.cakes, COST_FUNCTION.UNIT);
    return s;
  }
  
  @Override
  public boolean isGoal(State state) {
    return ((PancakeState)state).d == 0;
  }
  
  private int ngaps(int cakes[], COST_FUNCTION costFunction) {
    int gaps = 0;
    for (int i = 0; i < numCakes; i++) {
      if (gap(cakes, i)) {
        switch(costFunction) {
        case UNIT:
          gaps++;
          break;
        case HEAVY:
          int a = cakes[i];
          int b = (i!=numCakes-1) ? cakes[i+1] : Integer.MAX_VALUE;
          gaps += (1+Math.min(a, b));
          break;
        }
      }
    }
    return gaps;
  }
  
  // Is there a gap between cakes n and n+1?
  private boolean gap(int cakes[], int n) {
    if (n == numCakes-1)
      return cakes[numCakes-1] != numCakes-1;
    return Math.abs(cakes[n] - cakes[n+1]) != 1;
  }

  @Override
  public int getNumOperators(State state) {
    return numCakes - 1;
  }

  @Override
  public Operator getOperator(State state, int nth) {
    return oplookup[nth];
  }

  @Override
  public State applyOperator(State state, Operator op) {  
  	PancakeState ps = (PancakeState)copy(state);
  	int o = ((PancakeOperator)op).value;
    ps.flip(o);
    ps.h = ngaps(ps.cakes, costFunction);
    ps.d = ngaps(ps.cakes, COST_FUNCTION.UNIT);      
    return ps;
  }
  
  private double cost(int op) {
    double value = 1.0;
    switch(costFunction) {
    case HEAVY:
      value = 1+op;
      break;
    case UNIT:
    default:
    	break;
    }
    return value;
  }
  

  @Override
  public State copy(State state) {
    return new PancakeState((PancakeState)state);
  }

  @Override
  public long pack(State s) {
  	PancakeState ps = (PancakeState)s;
    long word = 0;
    for (int i = 0; i < numCakes; i++) {
      word = (word << 4) | ps.cakes[i];
    }
    return word;
  }
  
  @Override
  public State unpack(long word) {
  	PancakeState state = new PancakeState(numCakes);
    for (int i = numCakes - 1; i >= 0; i--) {
      int t = (int) word & 0xF;
      word >>= 4;
      state.cakes[i] = t;
    }
    state.h = ngaps(state.cakes, costFunction);
    return state;
  }
  
  /**
   * The operator class.
   */
  private final class PancakeOperator implements Operator {
  	
  	private int value;
  	
  	private PancakeOperator(int value) {
  		this.value = value;
  	}

  	@Override
  	public double getCost(State state) {
  		PancakeState ps = (PancakeState)state;
  		return cost(ps.cakes[value]);
  	}

  	@Override
  	public Operator reverse(State state) {
  		return this;
  	}
  	
  }
}

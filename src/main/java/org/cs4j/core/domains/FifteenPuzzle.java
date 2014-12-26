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
import java.util.ArrayList;
import java.util.List;

import org.cs4j.core.SearchDomain;

/**
 * The 4x4 sliding-tiles domain class.
 */
public final class FifteenPuzzle implements SearchDomain {
  
  private final int width = 4;
  private final int height = 4;
  private final int Ntiles = width * height;
  private int init[] = new int[Ntiles];
  
  private double md[][] = new double[Ntiles][Ntiles];
  private double mdincr[][][] = new double[Ntiles][Ntiles][Ntiles];
  private int md_unit[][] = new int[Ntiles][Ntiles];
  private int mdincr_unit[][][] = new int[Ntiles][Ntiles][Ntiles];    
  
  private int optab_n[] = new int[Ntiles]; 
  private int optab_ops[][] = new int[Ntiles][4]; 
  private Operator oplookup[] = new Operator[Ntiles];

  private COST_FUNCTION costFunction;
  
  public enum COST_FUNCTION {UNIT, SQRT, INVR, HEAVY};
    
  /**
   * The constructor reads a tiles problem instance from the specified
   * input stream.
   * 
   * @param stream the input stream
   */
  public FifteenPuzzle(InputStream stream) {
    this(stream, COST_FUNCTION.UNIT);
  }
  
  public FifteenPuzzle(InputStream stream, COST_FUNCTION cost) {
    try {
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(stream));
      String line = reader.readLine();
      
      String dim[] = line.split(" ");
      /*width =*/ Integer.parseInt(dim[0]);
      /*height =*/ Integer.parseInt(dim[0]);
      
      line = reader.readLine(); 
      for (int t = 0; t < Ntiles; t++) {
        int p = Integer.parseInt(reader.readLine());
        init[p] = t;
      }
      
      line = reader.readLine(); 
      for (int t = 0; t < Ntiles; t++) {
        int p = Integer.parseInt(reader.readLine());
        if (p != t)
          throw new IllegalArgumentException("Non-canonical goal positions");
      }
    }
    catch (IOException exception) {
      exception.printStackTrace();
    }

    init(cost);
    for (int i=0; i<oplookup.length; i++)
    	oplookup[i] = new FifteenPuzzleOperator(i);
  }
  
  private void init(COST_FUNCTION cost) {
    this.costFunction = cost;
    initmd();
    initmd_unit();
    initoptab();
  }
  
  @Override
  public State initialState() {
    int blank = -1;
    int one = -1;
    int tiles[] = new int[Ntiles];
    for (int i = 0; i < Ntiles; i++) {
      if (init[i] == 0) blank = i;
      else if (init[i] == 1) one = i;
      tiles[i] = init[i]; 
    }
    if (blank < 0)
        throw new IllegalArgumentException("No blank tile");
    TileState s = new TileState();
    s.tiles = tiles;
    s.blank = blank;
    s.one = one;    
    s.h = mdist(s.blank, s.tiles, costFunction);    
    s.d = mdist(s.blank, s.tiles, COST_FUNCTION.UNIT);    

    return s;
  }  
  
  @Override
  public boolean isGoal(State state) {
    return ((TileState)state).d == 0;
  }
  
  public List<Operator> getOperators(State s) {
  	TileState ts = (TileState)s;
  	int nops = optab_n[ts.blank];
  	List<Operator> list = new ArrayList<>(nops*2);
  	for (int i=0; i<nops; i++) {
  		Operator op = oplookup[optab_ops[ts.blank][i]];
  		list.add(op);
  	}
    return list;
  }
  
  @Override
  public int getNumOperators(State state) {
    return optab_n[((TileState)state).blank];
  }

  @Override
  public Operator getOperator(State s, int index) {
  	TileState ts = (TileState)s;
    return oplookup[optab_ops[ts.blank][index]];
  }
  
  @Override
  public State copy(State s) {
  	TileState ts = (TileState)s;
    TileState copy = new TileState();
    System.arraycopy(ts.tiles, 0, copy.tiles, 0, ts.tiles.length);
    copy.blank = ts.blank;
    copy.one = ts.one;
    copy.h = ts.h;
    copy.d = ts.d;
    return copy;
  }  
  
  /*
  @Override
  public Edge<State, Operator> apply(State s, Operator op) {
  	TileState ts = (TileState)s;
  	FifteenPuzzleOperator fop = (FifteenPuzzleOperator)op;
    int newb = (fop.value == -1) ? ts.blank : fop.value; 
    TileUndo undo = new TileUndo(ts);
    int tile = ts.tiles[newb];
    Edge<State, Operator> edge = 
        new EdgeImpl<State, Operator>(cost(tile), 
        		FifteenPuzzleOperator.lookup[ts.blank], undo);
    ts.tiles[ts.blank] = tile;
    if (tile == 1) ts.one = ts.blank;
    
    ts.h += mdincr[tile][newb][ts.blank];
    ts.d += mdincr_unit[tile][newb][ts.blank];
    ts.blank = newb;
     
    return edge;
  }
  */
  
  @Override
  public State applyOperator(State s, Operator op) {
  	TileState ts = (TileState)copy(s);
  	FifteenPuzzleOperator fop = (FifteenPuzzleOperator)op;
    int newb = fop.value; 
    int tile = ts.tiles[fop.value];
    ts.tiles[ts.blank] = tile;
    if (tile == 1) ts.one = ts.blank;    
    ts.h += mdincr[tile][newb][ts.blank];
    ts.d += mdincr_unit[tile][newb][ts.blank];
    ts.blank = newb;     
    return ts;
  }
  
  @Override
  public long pack(State s) {
  	TileState ts = (TileState)s;
    long word = 0;
    ts.tiles[ts.blank] = 0;
    for (int i = 0; i < Ntiles; i++) {
      word = (word << 4) | ts.tiles[i];
    }
    return word;
  }
  
  @Override
  public State unpack(long word) {
  	TileState dst = new TileState();
    dst.h = 0;
    dst.blank = -1;
    for (int i = Ntiles - 1; i >= 0; i--) {
      int t = (int) word & 0xF;
      word >>= 4;
      dst.tiles[i] = t;
      if (t == 0)
        dst.blank = i;
      else
        dst.h += md[t][i];
      if (t == 1)
        dst.one = i;
    }
    return dst;
  }  
 
  /*
   * Computes the Manhattan distance for the specified blank and tile
   * configuration.
   */
  private double mdist(int blank, int tiles[], COST_FUNCTION function) {
    double sum = 0;
    for (int i = 0; i < Ntiles; i++) {
      if (i == blank)
        continue;
      double cost = (function != COST_FUNCTION.UNIT) ? cost(tiles[i]):1.0;
      int row = i / width, col = i % width;
      int grow = tiles[i] / width, gcol = tiles[i] % width;
      sum += (Math.abs(gcol - col) + Math.abs(grow - row)) * cost;
    }
    return sum;
  }

  /*
   * The cost function.
   */
  private double cost(int tile) {
    double value = 1.0;
    switch(costFunction) {
    case HEAVY:
      value = tile;
      break;
    case SQRT: 
      value = Math.sqrt(tile); 
      break;
    case INVR: 
      value = 1.0d/(double)tile; 
      break;
    case UNIT:
      break;
    }
    return value;
  }
  
  /*
   * Initializes the Manhattan distance heuristic table.
   */
  private void initmd() {
    for (int t = 1; t < Ntiles; t++) {
      double cost = cost(t);
      int grow = t / width, gcol = t % width;
        for (int l = 0; l < Ntiles; l++) {
          int row = l / width, col = l % width;
          md[t][l] = (Math.abs(col - gcol) + Math.abs(row - grow)) * cost;
        }
    }
    for (int t = 1; t < Ntiles; t++) {
      for (int d = 0; d < Ntiles; d++) {
        double newmd = md[t][d];
        for (int s = 0; s < Ntiles; s++)
          mdincr[t][d][s] = -100; // some invalid value.                 
        if (d >= width)
          mdincr[t][d][d - width] = md[t][d - width] - newmd;
        if (d % width > 0)
          mdincr[t][d][d - 1] = md[t][d - 1] - newmd;
        if (d % width < width - 1)
          mdincr[t][d][d + 1] = md[t][d + 1] - newmd;
        if (d < Ntiles - width)
          mdincr[t][d][d + width] = md[t][d + width] - newmd;
      }
    }
  }
  
  /*
   * Initializes the Manhattan distance heuristic table.
   */
  private void initmd_unit() {
    for (int t = 1; t < Ntiles; t++) {
      int grow = t / width, gcol = t % width;
        for (int l = 0; l < Ntiles; l++) {
          int row = l / width, col = l % width;
          md_unit[t][l] = Math.abs(col - gcol) + Math.abs(row - grow);
        }
    }
    for (int t = 1; t < Ntiles; t++) {
      for (int d = 0; d < Ntiles; d++) {
        int newmd = md_unit[t][d];
        for (int s = 0; s < Ntiles; s++)
          mdincr_unit[t][d][s] = -100; // some invalid value.                 
        if (d >= width)
          mdincr_unit[t][d][d - width] = md_unit[t][d - width] - newmd;
        if (d % width > 0)
          mdincr_unit[t][d][d - 1] = md_unit[t][d - 1] - newmd;
        if (d % width < width - 1)
          mdincr_unit[t][d][d + 1] = md_unit[t][d + 1] - newmd;
        if (d < Ntiles - width)
          mdincr_unit[t][d][d + width] = md_unit[t][d + width] - newmd;
      }
    }
  }
    
  /*
   * Initializes the operators.
   */
  private void initoptab() {
    for (int i = 0; i < Ntiles; i++) {
      optab_n[i] = 0;
      if (i >= width)
              optab_ops[i][optab_n[i]++] =  i - width;
      if (i % width > 0)
              optab_ops[i][optab_n[i]++] =  i - 1;
      if (i % width < width - 1)
              optab_ops[i][optab_n[i]++] =  i + 1;
      if (i < Ntiles - width)
              optab_ops[i][optab_n[i]++] =  i + width;
      assert (optab_n[i] <= 4);
    }
  }  

  /**
   * The tile state class.
   */
  private static final class TileState implements State {  
    
  	private int tiles[] = new int[16];
    private int blank, one;
    private double h, d;
    
    @Override
    public double getH() {
    	return h;
    }
    
    @Override
    public double getD() {
    	return d;
    }
    
  }
  
  /**
   * The operator class.
   */
  private final class FifteenPuzzleOperator implements Operator {
  	  	
  	private int value;
  	
  	private FifteenPuzzleOperator(int value) {
  		this.value = value;
  	}
  	
  	@Override
  	public double getCost(State s) {
  		TileState ts = (TileState)s;
      int tile = ts.tiles[value];
      return cost(tile);
  	}
  	
  	@Override
  	public Operator reverse(State s) {
  		TileState ts = (TileState)s;
  		return oplookup[ts.blank];
  	}
  }
  
}

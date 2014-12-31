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
package org.cs4j.core.collections;

import java.util.ArrayList;
import java.util.List;

/**
 * A bucket heap.
 * 
 * @author Matthew Hatem
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class BucketHeap<T extends Heapable> implements Heap<T> {
	
  private int fill, size;
  private int min = Integer.MAX_VALUE;
  private Bucket[] buckets;
  
  public BucketHeap(int size) {
  	this.size = size;
    this.buckets = new Bucket[size];
  }
  
  @Override
  public void add(T n) {
    int p0 = (int)n.getRank(0);
    if (p0 < min) {
      min = p0;
    }
    Bucket<T> bucket = buckets[p0];
    if (bucket == null) {
      bucket = new Bucket<>(buckets.length);
      buckets[p0] = bucket;
    }
    int p1 = (int)n.getRank(1);
    bucket.push(n, p1);
    fill++;
  }
  
  public T poll() {
    for (; min < buckets.length; min++) {
      Bucket minBucket = buckets[min];
      if (minBucket != null && !minBucket.isEmpty()) break;
    }
    fill--;
    Bucket<T> minBucket = buckets[min];
    T t = minBucket.pop();
    t.setIndex(0, -1);
    t.setIndex(1, -1);
    return t;
  }
  
	@Override
	public void update(T t) {
		// find t
		int p0 = (int)t.getIndex(0);
		if (p0 > buckets.length-1 || p0 < 0)
			throw new IllegalArgumentException();
		Bucket<T> b = buckets[p0];
		int p1 = (int)t.getIndex(1);
		if (p1 > b.bins.length-1 || p1 < 0)
			throw new IllegalArgumentException();
		// remove t
		List<T> list = b.bins[p1];
		list.remove(t);
		// add t
		add(t);
	}

	@Override
	public void clear() {
		fill = 0;
		min = Integer.MAX_VALUE;
		buckets = new Bucket[size];
	}
  
  @Override
  public boolean isEmpty() { 
    return fill == 0; 
  }
  
  @Override
  public int size() {
  	return fill;
  }

  private static final class Bucket<T> {
    private int fill, max;
    private ArrayList[] bins;
    
    Bucket(int size) {
      bins = new ArrayList[size];
    }
        
    private void push(T n, int p) {
       if (p > max) {
        max = p; 
      }
      ArrayList<T> binP = bins[p];
      if (binP == null) {
        binP = new ArrayList<T>(10000);
        bins[p] = binP;
      }
      binP.add(n);
      fill++;
    }
    
    private T pop() {
      for ( ; max > 0; max--) {
        ArrayList<T> maxBin = bins[max];
        if (maxBin != null && !maxBin.isEmpty()) break;
      }
      ArrayList<T> maxBin = bins[max];
      int last = maxBin.size()-1;
      T n = maxBin.get(last);
      maxBin.remove(last);
      fill--;
      return n;
    }
    
    private boolean isEmpty() {
      return fill == 0;
    }
  }

}

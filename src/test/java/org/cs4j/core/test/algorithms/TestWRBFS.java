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
package org.cs4j.core.test.algorithms;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import junit.framework.Assert;

import org.cs4j.core.SearchAlgorithm;
import org.cs4j.core.SearchResult;
import org.cs4j.core.SearchResult.Solution;
import org.cs4j.core.algorithms.RBFS;
import org.cs4j.core.algorithms.WRBFS;
import org.cs4j.core.domains.FifteenPuzzle;
import org.junit.Test;

public class TestWRBFS {
	
	@Test
	public void testWRBFSFifteenPuzzleUnit_12() {
		try {
			InputStream is = new FileInputStream(new File("input/fifteenpuzzle/korf100/12"));
			FifteenPuzzle puzzle = new FifteenPuzzle(is);
			SearchAlgorithm algo = new RBFS();			
			SearchResult result = algo.search(puzzle);
			Solution sol = result.getSolutions().get(0);
			
			System.out.println(result);
			
			Assert.assertTrue(result.getWallTimeMillis() > 1);
			Assert.assertTrue(result.getWallTimeMillis() < 200);
			Assert.assertTrue(result.getCpuTimeMillis() > 1);
			Assert.assertTrue(result.getCpuTimeMillis() < 200);
			Assert.assertTrue(result.getGenerated() == 301098);
			Assert.assertTrue(result.getExpanded() == 148421);			
			Assert.assertTrue(sol.getCost() == 45);
			Assert.assertTrue(sol.getLength() == 45+1);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();			
		}
	}
	@Test
	public void testWRBFSFifteenPuzzleUnit_45() {
		try {
			InputStream is = new FileInputStream(new File("input/fifteenpuzzle/korf100/45"));
			FifteenPuzzle puzzle = new FifteenPuzzle(is);
						
			SearchAlgorithm algo = new WRBFS(2.0);
			SearchResult result = algo.search(puzzle);
			System.out.println(result);
			
			algo = new RBFS(2.0);
			result = algo.search(puzzle);
			System.out.println(result);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();			
		}
	}
	
	public static void main(String[] args) {
		TestWRBFS test = new TestWRBFS();
		test.testWRBFSFifteenPuzzleUnit_12();
		test.testWRBFSFifteenPuzzleUnit_45();
	}

}

package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GraphExtractor {
	
	private Draw draw;
	private BufferedReader reader;
	
	private HashMap<Integer, Boolean> graph;
	private HashSet<Integer> nodesInGraph;

	private HashMap<Integer, Node> IDsToNodes;
	
	private int vertexCount;
	
	public GraphExtractor(String path) throws IOException {
		
		draw = new Draw();
		
		reader = new BufferedReader(new FileReader(path));
		String line = null;
		
		IDsToNodes = new HashMap<Integer, Node>();
		graph = new HashMap<Integer, Boolean>();
		// The guy above's just gonna be true's all the way down; it's for the 
		// bonus function to change the values around
		
		// Actually JK I'm gonna change it too
		
		nodesInGraph = new HashSet<Integer>();
		
		
		HashMap<Integer, HashSet<Integer>> nodeToNeighbors = 
				new HashMap<Integer, HashSet<Integer>>();
		
		vertexCount = 0;
		
		while ((line = reader.readLine()) != null) {
			
			String[] parts = line.split("\\s");
			
			if (parts[0].equals("p")) {
				vertexCount = Integer.parseInt(parts[2]);
				// Initialize
				for (int i=0; i<vertexCount; i++) {
					graph.put(i, true);
					nodeToNeighbors.put(i, new HashSet<Integer>());
				}
			}
			else {
				// They use 1-indexing, I use 0-indexing, hence the minus one
				int vertex1id = Integer.parseInt(parts[1]) - 1;
				int vertex2id = Integer.parseInt(parts[2]) - 1;
				nodeToNeighbors.get(vertex1id).add(vertex2id);
				nodeToNeighbors.get(vertex2id).add(vertex1id);
			}
		}
		
		for (int i=0; i<vertexCount; i++) {
			IDsToNodes.put(i, new Node(nodeToNeighbors.get(i), i)); // Create all the nodes
			nodesInGraph.add(i);
			
		}
		
		HashMap<Integer, Boolean> pristineGraph = (HashMap<Integer, Boolean>) graph.clone();
		HashSet<Integer> pristineNodes = (HashSet<Integer>) nodesInGraph.clone();
		
	
		Pair<List<Integer>, Double> finalIncludedResult = findIndependentSetTruncation();
//		Pair<List<Integer>, Double> finalIncludedResult = repeatRandomGreedy();
//		Pair<List<Integer>, Double> finalIncludedResult = machineLearningGreedy(nodesInGraph, 1, 1); // TODO
		
		graph = (HashMap<Integer, Boolean>) pristineGraph.clone();
		nodesInGraph = (HashSet<Integer>) pristineNodes.clone();
		
		List<Integer> finalIncludedList = finalIncludedResult.first;
		double finalIncludedValue = finalIncludedResult.second;
		
		HashSet<Integer> finalIncludedSet = new HashSet<Integer>();
		finalIncludedSet.addAll(finalIncludedList);
		
		drawTheBoardFromFinalSet(finalIncludedSet);
		
		Pair<List<Integer>, Double> greedyResult = repeatRandomGreedy();
		
		graph = (HashMap<Integer, Boolean>) pristineGraph.clone();
		nodesInGraph = (HashSet<Integer>) pristineNodes.clone();
		
		Pair<List<Integer>, Double> mlResult = machineLearningGreedy(nodesInGraph, 
				0, 1000);
		
		List<Integer> greedyList = greedyResult.first;
		double greedyValue = greedyResult.second;
		
		HashSet<Integer> greedySet = new HashSet<Integer>();
		greedySet.addAll(greedyList);
		
		drawTheBoardFromFinalSet(greedySet);
		
		System.out.println("BONUS:");
		System.out.println("INCLUDED " + finalIncludedList.size() + " NODES,");
		System.out.println("ACHIEVING A VALUE OF " + finalIncludedValue);
		
		System.out.println("GREEDY: " + greedyList.size() + " " + greedyValue);
		
		System.out.println("ML: " + mlResult.first.size() + " " + mlResult.second);
		
		for (int i=0; i<finalIncludedList.size(); i++) {
			System.out.println(finalIncludedList.get(i));
		} 
		
//		System.out.println(countTriangles());
	}
	
	@SuppressWarnings("unchecked")
	private Pair<List<Integer>, Double> findIndependentSetTruncation() {
		int depth = 3; // TODO
		
		boolean depthTooSmall = false;
		
		List<Integer> listOfIncludedNodeIDs = new ArrayList<Integer>();
		double totalValue = 0.0;
		
		// These below are going to be used to check convergence
		HashMap<Integer, Double> idsToBonuses;
		HashMap<Integer, Double> oldIDsToBonuses;
		
		// This is used to cut the converging off if it's taking forever
		int iterationsSoFar;
		int MAX_ITERATIONS = 30; // TODO
		
		reinitializeAllNodes(nodesInGraph);
		
		String MODE = "WEIGHTED_GRAPH"; // TODO
				
		while (!nodesInGraph.isEmpty()) {
			System.out.println("NEXT RUN-THROUGH");
			
			idsToBonuses = new HashMap<Integer, Double>();
			oldIDsToBonuses = new HashMap<Integer, Double>();
			oldIDsToBonuses.put(0, 0.0);
			
			iterationsSoFar = 0;
			
			while (!idsToBonuses.equals(oldIDsToBonuses) && (iterationsSoFar < MAX_ITERATIONS)) {
				
				for (int i=0; i<vertexCount; i++) {
					IDsToNodes.get(i).resetBonus();
				} 
				
				iterationsSoFar++;
				oldIDsToBonuses = (HashMap<Integer, Double>) idsToBonuses.clone();
				
				// Iterate over every node and find its approximate bonus.

				for (int i=0; i<vertexCount; i++) {
					if (graph.get(i)) {

						Node nodeBeingComputed = IDsToNodes.get(i);
						
						// Third argument: DEPTH
						double bonusEstimate;
						
						bonusEstimate = nodeBeingComputed.approximateBonus(IDsToNodes, graph, depth);
					
						nodeBeingComputed.bonusEstimate = bonusEstimate;
						idsToBonuses.put(i, bonusEstimate);
					
 						System.out.println("" + i + ", " + bonusEstimate);
					}
				}
			}
			
			// If we have a weighted graph, we ignore all of the nodesThatAreOn stupidity.
			if (MODE.equals("WEIGHTED_GRAPH")) {
				
				double bestBonus = -0.1;
				int bestID = -1;
				
				for (int id : idsToBonuses.keySet()) {
					
					if (idsToBonuses.get(id) > bestBonus) {
						bestBonus = idsToBonuses.get(id);
						bestID = id;
					}
				}
				
				include(bestID, IDsToNodes, listOfIncludedNodeIDs);
				totalValue += IDsToNodes.get(bestID).weight;
			}
			
			else {
				// Set of IDs of nodes that are on.
				HashSet<Integer> nodesThatAreOn = new HashSet<Integer>();
				
				// Iterate over every node; take every node that's definitely on, and include 
				// it. Throw its neighbors into the ocean, as an offering to the Drowned God.
				for (int i=0; i<vertexCount; i++) {
					if (graph.get(i)) {
						if (IDsToNodes.get(i).bonusEstimate == 1.0) {
							nodesThatAreOn.add(i);
						}
					}
				}
				
				reinitializeAllNodes(nodesInGraph);
				
				if (!nodesInGraph.isEmpty()) {
				
					// If nodesThatAreOn is empty, we have to eliminate one of the nodes in the graph
					// at random, and repeat.
					if (nodesThatAreOn.isEmpty()) {
						delete(randomChoose(nodesInGraph));
	//					include(findIDofNodeWithHighestBonus(), IDsToNodes, listOfIncludedNodeIDs);
					}
					
					// In this case, we have to check that the included nodes make sense. 
					// (In particular, no two included nodes can be adjacent)
					// If they do,
					// we delete each included node and all its neighbors
					
					else {
	
						depthTooSmall = false;
						
						Iterator<Integer> nodesThatAreOnIterator = nodesThatAreOn.iterator();
						
						// Check every pair of nodes for adjacency.
						// factor of 2 optimization still possible
						while (nodesThatAreOnIterator.hasNext()) {
							int nextNodeID = nodesThatAreOnIterator.next();
							
							Iterator<Integer> nodesThatAreOnIterator2 = nodesThatAreOn.iterator();
							
							while (nodesThatAreOnIterator2.hasNext()) {
								int nextNodeID2 = nodesThatAreOnIterator2.next();
								
								if (IDsToNodes.get(nextNodeID).neighbors.contains(nextNodeID2)) {
									
									
									// Find the independent set of the graph of the conflict nodes,
									// and include that.
									
									if (MODE.equals("RECURSE")) {
										
										System.out.println("COLLISION: " + nextNodeID + " " + nextNodeID2);
										
										HashMap<Integer, Boolean> conflictGraph = 
												new HashMap<Integer, Boolean>();
										HashSet<Integer> conflictNodesInGraph = 
												new HashSet<Integer>();
										
										for (int i=0; i<vertexCount; i++) {
											if (nodesThatAreOn.contains(i)) {
												conflictGraph.put(i, true);
												conflictNodesInGraph.add(i);
											}
											else {
												conflictGraph.put(i, false);
											}
										}
										
										HashMap<Integer, Boolean> oldGraph = 
												(HashMap<Integer, Boolean>) graph.clone();
										HashSet<Integer> oldNodesInGraph =
												(HashSet<Integer>) nodesInGraph.clone();
										
										graph = conflictGraph;
										nodesInGraph = conflictNodesInGraph;
										
										// Recursion step
										Pair<List<Integer>, Double> resultOfNodesToBeIncluded = findIndependentSetTruncation();
										
										List<Integer> listOfNodesToBeIncluded = resultOfNodesToBeIncluded.first;
										totalValue += resultOfNodesToBeIncluded.second;
										
										graph = oldGraph;
										nodesInGraph = oldNodesInGraph;
										
										for (int i=0; i<listOfNodesToBeIncluded.size(); i++) {
											totalValue += IDsToNodes.get(listOfNodesToBeIncluded.get(i)).weight;
											
											include(listOfNodesToBeIncluded.get(i), IDsToNodes, 
													listOfIncludedNodeIDs);
										}
										
										depthTooSmall = true;
										break;
									}
									
									else if (MODE.equals("RECURSE_WITH_RANDOM_CHOICE")) {
										System.out.println("COLLISION: " + nextNodeID + " " + nextNodeID2);
										
										HashMap<Integer, Boolean> conflictGraph = 
												new HashMap<Integer, Boolean>();
										HashSet<Integer> conflictNodesInGraph = 
												new HashSet<Integer>();
										
										for (int i=0; i<vertexCount; i++) {
											if (nodesThatAreOn.contains(i)) {
												conflictGraph.put(i, true);
												conflictNodesInGraph.add(i);
											}
											else {
												conflictGraph.put(i, false);
											}
										}
										
										HashMap<Integer, Boolean> oldGraph = 
												(HashMap<Integer, Boolean>) graph.clone();
										HashSet<Integer> oldNodesInGraph =
												(HashSet<Integer>) nodesInGraph.clone();
										
										graph = conflictGraph;
										nodesInGraph = conflictNodesInGraph;
										
										Pair<List<Integer>, Double> resultOfNodesToBeIncluded = 
												findIndependentSetTruncation();
										
										
										// Recursion step
										List<Integer> listOfCandidateNodesToBeIncluded = 
												resultOfNodesToBeIncluded.first;
										
										graph = oldGraph;
										nodesInGraph = oldNodesInGraph;
										
										int chosenNode = randomChoose(listOfCandidateNodesToBeIncluded);
										
										totalValue += IDsToNodes.get(chosenNode).weight;
										
										include(randomChoose(listOfCandidateNodesToBeIncluded), IDsToNodes, 
												listOfIncludedNodeIDs);
										
										depthTooSmall = true;
										break;
									}
									
									else if (MODE.equals("INCREASE_DEPTH")) {
									
										depth += 1;
										System.out.println("COLLISION: " + nextNodeID + " " + nextNodeID2);
		 								System.out.println("DEPTH " + (depth - 1) + " TOO SMALL");
										depthTooSmall = true;
										break;
									
									}
									else if (MODE.equals("RANDOM_CHOOSE")) {
										
										System.out.println("COLLISION: " + nextNodeID + " " + nextNodeID2);
										
										depthTooSmall = true;
										
										int chosenNode = randomChoose(nodesThatAreOn);
										
										totalValue += IDsToNodes.get(chosenNode).weight;
										
										include(chosenNode, IDsToNodes, 
												listOfIncludedNodeIDs);
										
										break;
									}
									
									else if (MODE.equals("SHOW_COLLISIONS")) {
										System.out.println("COLLISION: " + nextNodeID + " " + nextNodeID2);
									}
								}
							}
							
							if (depthTooSmall) {
								break;
							}
						}
						
						if (!depthTooSmall) {
						
							nodesThatAreOnIterator = nodesThatAreOn.iterator();
						
							while ((nodesThatAreOnIterator.hasNext()) && (!depthTooSmall)) {
								int nextNodeID = nodesThatAreOnIterator.next();
								
								include(nextNodeID, IDsToNodes, listOfIncludedNodeIDs);
							} 
						
	//						include(randomChoose(nodesThatAreOn), IDsToNodes, listOfIncludedNodeIDs);
						}
					}
				}
			}
		}
		
		return new Pair<List<Integer>, Double>(listOfIncludedNodeIDs, totalValue);
	}
	
	public List<Integer> adamsExperiment() {
		int ITERATION_NUM = 100;
		
		List<Integer> listOfIncludedNodeIDs = new ArrayList<Integer>();
		
		while (!nodesInGraph.isEmpty()) {
			setAllNodesBonusTo(1.0 / 450.0);
			for (int i=0; i<ITERATION_NUM; i++) {
				updateAllNodesBonusAsAdamWould();
				resetAllBonuses();
				
				double bonusSum = sumOfBonuses();
				scaleAllBonusesBy(1.0 / bonusSum);
			}
			
			for (int nodeID : nodesInGraph) {
				System.out.println(IDsToNodes.get(nodeID).bonusEstimate);
			}
			
			int IDofNodeWithHighestBonus = findIDofNodeWithHighestBonus();
			include(IDofNodeWithHighestBonus, IDsToNodes, listOfIncludedNodeIDs);
		}
		
		return listOfIncludedNodeIDs;
	}
	
	public double sumOfBonuses() {
		double counter = 0.0;
		
		for (int nodeID : nodesInGraph) {
			counter += IDsToNodes.get(nodeID).bonusEstimate;
		}
		
		return counter;
	}
	
	public int findIDofNodeWithHighestBonus() {
		double highestBonus = -100000;
		int IDofNodeWithHighestBonus = -1;
		
		for (int nodeID : nodesInGraph) {
			if (IDsToNodes.get(nodeID).bonusEstimate > IDofNodeWithHighestBonus) {
				IDofNodeWithHighestBonus = nodeID;
				highestBonus = IDsToNodes.get(nodeID).bonusEstimate;
			}
		}
		
		return IDofNodeWithHighestBonus;
	}
	
	public void scaleAllBonusesBy(double value) {
		for (int nodeID : nodesInGraph) {
			IDsToNodes.get(nodeID).bonusEstimate *= value;
			IDsToNodes.get(nodeID).oldBonusEstimate *= value;
		}
	}
	
	public void updateAllNodesBonusAsAdamWould() {
		for (int nodeID : nodesInGraph) {
			IDsToNodes.get(nodeID).setBonusLikeAdamWould(IDsToNodes);
		}
	}
	
	public void setAllNodesBonusTo(double value) {
		for (int nodeID : nodesInGraph) {
			IDsToNodes.get(nodeID).setBonusTo(value);
		}
	}
	
	public void resetAllBonuses() {
		for (int nodeID : nodesInGraph) {
			IDsToNodes.get(nodeID).resetBonus();
		}
	}
	
	public int countTriangles() {
		int triangleCount = 0;
		
		for (int a=0; a<vertexCount; a++) {
			for (int b=a+1; b<vertexCount; b++) {
				for (int c=b+1; c<vertexCount; c++) {
					if (IDsToNodes.get(a).neighbors.contains(b) &&
							IDsToNodes.get(b).neighbors.contains(c) && 
							IDsToNodes.get(c).neighbors.contains(a)) {
						
						System.out.println("" + a + ", " + b + ", " + c);
						
						triangleCount++;
					}
				}
			}
		}
		
		return triangleCount;
	}
	
	@SuppressWarnings("unchecked")
	private List<Integer> machineLearningFuzzy() {
		double LAXNESS = 1;
		Random rand = new Random();
		
		HashSet<Integer> masterCopy = (HashSet<Integer>) nodesInGraph.clone();
		HashSet<Integer> nodesInGraphClone = (HashSet<Integer>) nodesInGraph.clone();
		
		HashMap<Integer, Double> nodeIDtoTotalIndSetSize = new HashMap<Integer, Double>();
		HashMap<Integer, Integer> nodeIDtoNumIndSets = new HashMap<Integer, Integer>();
		HashMap<Integer, Double> nodeIDtoAvgIndSetSize = new HashMap<Integer, Double>();
		
		// Initialize the hashmaps
		for (int i : masterCopy) {
			nodeIDtoTotalIndSetSize.put(i, 0.0);
			nodeIDtoNumIndSets.put(i, 0);
			nodeIDtoAvgIndSetSize.put(i, 0.0);
		}
		
		List<Integer> masterList = new ArrayList<Integer>();
		
		double maxAvgIndSetSize = 0;
		
		int candidateNode;
		double candidateAvg;
		double diff;
		double rerollProb = 0;
		
		double currentTotal;
		int currentNum;
		
		double avgIndSetSize;
		
		for (int i=0; i<1000; i++) {
			
			for (int j=0; j<1000; j++) {
				List<Integer> listOfIncludedNodeIDs = new ArrayList<Integer>();
				nodesInGraphClone = (HashSet<Integer>) masterCopy.clone();
				
				if (maxAvgIndSetSize == 0) {
										
					while (!nodesInGraphClone.isEmpty()) {
						includeWhereYouSpecifyTheGraph(randomChoose(nodesInGraphClone), nodesInGraphClone, 
								graph, listOfIncludedNodeIDs, false);
					}
				}
				
				else {
										
					while (!nodesInGraphClone.isEmpty()) {
						// Do the weighted inclusion
						while (true) {
							candidateNode = randomChoose(nodesInGraphClone);
							candidateAvg = nodeIDtoAvgIndSetSize.get(candidateNode);
							diff = candidateAvg - maxAvgIndSetSize;
						
							if (diff == 0) 
								rerollProb = 0;
							
							else 
								rerollProb = Math.exp(LAXNESS*candidateAvg/diff);
							
							// This is flipped; this is because we enter this if statement when we
							// DON'T reroll.
//							System.out.println("reroll " + rerollProb + " " + candidateAvg + " " + 
	//						maxAvgIndSetSize);
							
							if (rand.nextDouble() > rerollProb) {
								break;
							}
						}
						
						includeWhereYouSpecifyTheGraph(candidateNode, nodesInGraphClone, 
								graph, listOfIncludedNodeIDs, false);
					}
				}
				
				for (int k : listOfIncludedNodeIDs) {
					currentTotal = nodeIDtoTotalIndSetSize.get(k);
					
					nodeIDtoTotalIndSetSize.put(k, currentTotal + listOfIncludedNodeIDs.size());
					currentNum = nodeIDtoNumIndSets.get(k);
										
					nodeIDtoNumIndSets.put(k, currentNum + 1);
				}
			}
			
			maxAvgIndSetSize = 0;
			
			for (int j : masterCopy) {
				System.out.println("" + j + " " + nodeIDtoTotalIndSetSize.get(j) + nodeIDtoNumIndSets.get(j));
				System.out.println("" + j + " " + nodeIDtoTotalIndSetSize.get(j) / nodeIDtoNumIndSets.get(j));
				System.out.println("");
				
				avgIndSetSize = nodeIDtoTotalIndSetSize.get(j) / nodeIDtoNumIndSets.get(j);
				
				nodeIDtoAvgIndSetSize.put(j, avgIndSetSize);
				
				if (avgIndSetSize > maxAvgIndSetSize) {
					maxAvgIndSetSize = avgIndSetSize;
				}
			}
		}
		
		while (!masterCopy.isEmpty()) {
		
			double bestAvg = 0;
			
			double avg;
			int bestNodeID = -1;
					
			for (int j : masterCopy) {
				avg = nodeIDtoAvgIndSetSize.get(j);
				System.out.println("" + j + " " + avg);
				
				if (nodeIDtoAvgIndSetSize.get(j) > bestAvg) {
					bestAvg = avg;
					bestNodeID = j;
				}
			}
			
			includeWhereYouSpecifyTheGraph(bestNodeID, masterCopy, graph, masterList, true);
		
		}
		
		return masterList;
	}
	
	@SuppressWarnings("unchecked")
	private Pair<List<Integer>, Double> machineLearningGreedy(HashSet<Integer> nodesInGraph, 
			int depth, int numberOfIterations) {
		
		HashSet<Integer> masterCopy = (HashSet<Integer>) nodesInGraph.clone();
		HashSet<Integer> nodesInGraphClone = (HashSet<Integer>) nodesInGraph.clone();
		
		HashMap<Integer, Double> nodeIDtoTotalIndSetValue = new HashMap<Integer, Double>();
		HashMap<Integer, Integer> nodeIDtoNumIndSets = new HashMap<Integer, Integer>();
		HashMap<Integer, Double> nodeIDtoAvgIndSetValue = new HashMap<Integer, Double>();
		
		HashMap<Integer, Double> nodeIDtoMaxIndSetValue = new HashMap<Integer, Double>();
		
		List<Integer> masterList = new ArrayList<Integer>();
		double masterValue = 0.0;
		
		String MODE = "";
		if (depth == 0) {
			MODE = "AVG";
		}
		else {
			MODE = "MAX";
		}
		
		while (!masterCopy.isEmpty()) {
			
			if (MODE.equals("AVG")) {
				for (int i : masterCopy) {
					nodeIDtoTotalIndSetValue.put(i, 0.0);
					nodeIDtoNumIndSets.put(i, 0);
					nodeIDtoAvgIndSetValue.put(i, 0.0);
				}
			}
			
			if (MODE.equals("MAX")) {
				for (int i : masterCopy) {
					nodeIDtoMaxIndSetValue.put(i, 0.0);
				}
			}
			
			for (int i=0; i<numberOfIterations; i++) {
				
				List<Integer> listOfIncludedNodeIDs = null;
				double valueOfIndSet = 0.0;
				
				nodesInGraphClone = (HashSet<Integer>) masterCopy.clone();
				
				if (depth == 0) {
					Pair<List<Integer>, Double> greedyResult = findIndependentSetGreedy(nodesInGraphClone);
					
					listOfIncludedNodeIDs = greedyResult.first;
					valueOfIndSet = greedyResult.second;
				}
				
				else {
					Pair<List<Integer>, Double> recurseResult = 
							machineLearningGreedy(masterCopy, depth - 1, 10);
					
					listOfIncludedNodeIDs = recurseResult.first;
					valueOfIndSet = recurseResult.second;
				}
								
				if (MODE.equals("AVG")) {
					// Update the stored average values
					for (int j : listOfIncludedNodeIDs) {
						double currentTotal = nodeIDtoTotalIndSetValue.get(j);
												
						nodeIDtoTotalIndSetValue.put(j, currentTotal + valueOfIndSet);
						int currentNum = nodeIDtoNumIndSets.get(j);
											
						nodeIDtoNumIndSets.put(j, currentNum + 1);
					}
				}
				
				if (MODE.equals("MAX")) {
					for (int j : listOfIncludedNodeIDs) {
						double currentMax = nodeIDtoMaxIndSetValue.get(j);
						if (valueOfIndSet > currentMax) {
							nodeIDtoMaxIndSetValue.put(j, valueOfIndSet);
						}
					}
				}
			}
			
			int bestNodeID = -1;
			
			if (MODE.equals("AVG")) {
				for (int j : masterCopy) {
					System.out.println("" + j + " " + nodeIDtoTotalIndSetValue.get(j) + " " 
							+ nodeIDtoNumIndSets.get(j));
					System.out.println("" + j + " " + nodeIDtoTotalIndSetValue.get(j) / nodeIDtoNumIndSets.get(j));
					System.out.println("");
					nodeIDtoAvgIndSetValue.put(j, 
							nodeIDtoTotalIndSetValue.get(j) / nodeIDtoNumIndSets.get(j));
				}
			
				double bestAvg = 0;
			
				double avg;
					
				for (int j : masterCopy) {
					avg = nodeIDtoAvgIndSetValue.get(j);
					System.out.println("" + j + " " + avg);
				
					if (avg > bestAvg) {
						bestAvg = avg;
						bestNodeID = j;
					}
				}
			}
			
			if (MODE.equals("MAX")) {			
				double bestMax = 0;
			
				double max;
					
				for (int j : masterCopy) {
					max = nodeIDtoMaxIndSetValue.get(j);
					System.out.println("" + j + " " + max);
				
					if (max > bestMax) {
						bestMax = max;
						bestNodeID = j;
					}
				}
			}
			
			includeWhereYouSpecifyTheGraph(bestNodeID, masterCopy, masterList, true);
			masterValue += IDsToNodes.get(bestNodeID).weight;
			
			System.out.println("STEP " + nodesInGraph.size());
		}	
		
		return new Pair<List<Integer>, Double>(masterList, masterValue);
	}
	
	@SuppressWarnings("unchecked")
	private Pair<List<Integer>, Double> repeatRandomGreedy() {
		List<Integer> bestIndSetSoFar = null;
		double bestIncludeCountSoFar = 0.0;
		
		HashSet<Integer> nodesInGraphClone = (HashSet<Integer>) nodesInGraph.clone();
		
		for (int i=0; i<10000; i++) {
			nodesInGraph = (HashSet<Integer>) nodesInGraphClone.clone();
			
			Pair<List<Integer>, Double> greedyResult = findIndependentSetGreedy();
						
			List<Integer> candidateList = greedyResult.first;
			double includeCount = greedyResult.second;
			
			System.out.println(includeCount);
			
			if (includeCount > bestIncludeCountSoFar) {
				bestIncludeCountSoFar = includeCount;
				bestIndSetSoFar = candidateList;
			}
		}
		
		return new Pair<List<Integer>, Double>(bestIndSetSoFar, bestIncludeCountSoFar);
	}
	
	private Pair<List<Integer>, Double> findIndependentSetGreedy() {
		List<Integer> listOfIncludedNodeIDs = new ArrayList<Integer>();
		
		double weightCounter = 0.0;
		
		while (!nodesInGraph.isEmpty()) {
			
			int chosenNode = randomChoose(nodesInGraph);
			
			include(chosenNode, IDsToNodes, 
					listOfIncludedNodeIDs);
			
			weightCounter += IDsToNodes.get(chosenNode).weight;
		}
		
		return new Pair<List<Integer>, Double>(listOfIncludedNodeIDs, weightCounter);
	}
	
	private Pair<List<Integer>, Double> findIndependentSetGreedy(HashSet<Integer> nodesInGraph) {
		List<Integer> listOfIncludedNodeIDs = new ArrayList<Integer>();
		
		double weightCounter = 0.0;
		
		while (!nodesInGraph.isEmpty()) {
			int chosenNode = randomChoose(nodesInGraph);
			
			includeWhereYouSpecifyTheGraph(randomChoose(nodesInGraph), nodesInGraph, 
					listOfIncludedNodeIDs, false);
			
			weightCounter += IDsToNodes.get(chosenNode).weight;
		}
		
		return new Pair<List<Integer>, Double>(listOfIncludedNodeIDs, weightCounter);
	}
	
	private Pair<List<Integer>, Double> findIndependentSetGreedy(HashSet<Integer> nodesInGraph,
			HashMap<Integer, Boolean> graph) {
		List<Integer> listOfIncludedNodeIDs = new ArrayList<Integer>();
		
		double weightCounter = 0.0;
		
		while (!nodesInGraph.isEmpty()) {
			int chosenNode = randomChoose(nodesInGraph);
			
			includeWhereYouSpecifyTheGraph(randomChoose(nodesInGraph), nodesInGraph, 
					graph, listOfIncludedNodeIDs, false);
			
			weightCounter += IDsToNodes.get(chosenNode).weight;
		}
		
		return new Pair<List<Integer>, Double>(listOfIncludedNodeIDs, weightCounter);
	}
	
	private Pair<List<Integer>, Double> findIndependentSetGreedyWithDegrees() {
		List<Integer> listOfIncludedNodeIDs = new ArrayList<Integer>();
		
		int bestDegree;
		int bestNode;
		double weightCounter = 0.0;
		
		int degree;
		
		while (!nodesInGraph.isEmpty()) {
			bestDegree = vertexCount;
			bestNode = 0;
			
			// Find the vertex with the lowest degree; doing an argmax by hand
			for (int i=0; i<vertexCount; i++) {
				if (graph.get(i)) {
					degree = IDsToNodes.get(i).countLiveNeighbors(graph);
					if (degree < bestDegree) {
						bestNode = i;
					}
				}
			}
			
			include(bestNode, IDsToNodes, listOfIncludedNodeIDs);
			
			weightCounter += IDsToNodes.get(bestNode).weight;
		}
		
		return new Pair<List<Integer>, Double>(listOfIncludedNodeIDs, weightCounter);
	}
	
	private void delete(int IDofNodeToBeDeleted) {
		graph.put(IDofNodeToBeDeleted, false);
		nodesInGraph.remove(IDofNodeToBeDeleted);
	}
	
	private void include(int IDofNodeToBeIncluded, HashMap<Integer, Node> IDsToNodes,
			List<Integer> listOfIncludedNodeIDs) {
		// delete neighbors
		Iterator<Integer> neighborsIterator = 
				IDsToNodes.get(IDofNodeToBeIncluded).neighbors.iterator();
		
		while (neighborsIterator.hasNext()) {
			int nextNeighborID = neighborsIterator.next();
			
			delete(nextNeighborID);
		}
		
		// delete the node itself
		delete(IDofNodeToBeIncluded);
		
		System.out.println("Included " + IDofNodeToBeIncluded);
		listOfIncludedNodeIDs.add(IDofNodeToBeIncluded);
	}
		
	private void includeWhereYouSpecifyTheGraph(int IDofNodeToBeIncluded, 
			HashSet<Integer> nodesInGraph, HashMap<Integer, Boolean> graph,
			List<Integer> listOfIncludedNodeIDs, Boolean verbose) {
		// delete neighbors
		
		Iterator<Integer> neighborsIterator = 
				IDsToNodes.get(IDofNodeToBeIncluded).neighbors.iterator();
		
		while (neighborsIterator.hasNext()) {
			int nextNeighborID = neighborsIterator.next();
			
			deleteWhereYouSpecifyTheGraph(nextNeighborID, nodesInGraph, graph);
		}
		
		// delete the node itself
		deleteWhereYouSpecifyTheGraph(IDofNodeToBeIncluded, nodesInGraph, graph);
		
		if (verbose)
			System.out.println("Included " + IDofNodeToBeIncluded);
		
		listOfIncludedNodeIDs.add(IDofNodeToBeIncluded);
		
	}
	
	private void includeWhereYouSpecifyTheGraph(int IDofNodeToBeIncluded, 
			HashSet<Integer> nodesInGraph,
			List<Integer> listOfIncludedNodeIDs, Boolean verbose) {
		// delete neighbors
		
		Iterator<Integer> neighborsIterator = 
				IDsToNodes.get(IDofNodeToBeIncluded).neighbors.iterator();
		
		while (neighborsIterator.hasNext()) {
			int nextNeighborID = neighborsIterator.next();
			
			deleteWhereYouSpecifyTheGraph(nextNeighborID, nodesInGraph);
		}
		
		// delete the node itself
		deleteWhereYouSpecifyTheGraph(IDofNodeToBeIncluded, nodesInGraph);
		
		if (verbose)
			System.out.println("Included " + IDofNodeToBeIncluded);
		
		listOfIncludedNodeIDs.add(IDofNodeToBeIncluded);
		
	}
		
	private void deleteWhereYouSpecifyTheGraph(int IDofNodeToBeDeleted,
			HashSet<Integer> nodesInGraph, HashMap<Integer, Boolean> graph) {
		graph.put(IDofNodeToBeDeleted, false);
		nodesInGraph.remove(IDofNodeToBeDeleted);
	}
	
	private void deleteWhereYouSpecifyTheGraph(int IDofNodeToBeDeleted,
			HashSet<Integer> nodesInGraph) {
		nodesInGraph.remove(IDofNodeToBeDeleted);
	}
	
	public <E> E randomChoose(HashSet<E> set) {
		@SuppressWarnings("unchecked")
		E[] choiceArray = (E[]) set.toArray();
		Random rand = new Random();
		return choiceArray[rand.nextInt(choiceArray.length)];
	}
	
	public <E> E randomChoose(List<E> list) {
		@SuppressWarnings("unchecked")
		E[] choiceArray = (E[]) list.toArray();
		Random rand = new Random();
		return choiceArray[rand.nextInt(choiceArray.length)];
	}
	
	private void reinitializeAllNodes(HashSet<Integer> nodesInGraph) {
		for (int i=0; i<vertexCount; i++) {
			IDsToNodes.get(i).reinitializeBonus(nodesInGraph);
		}
	}
	
	private void drawTheBoardFromFinalSet(HashSet<Integer> nodesInSet) {
		int SIDELENGTH = 20;
		
		draw.clear();
		
		for (int i=0; i<SIDELENGTH; i++) {
			for (int j=0; j<SIDELENGTH; j++) {
				int currentNodeID = SIDELENGTH * i + j;
				
				double x = (double) i;
				double y = (double) j;
				
				// Make a black circle
				if (nodesInSet.contains(currentNodeID)) {
					draw.filledCircle(x / SIDELENGTH, y / SIDELENGTH, 
							0.5 / SIDELENGTH);
				}
				// Make a white circle
				else { 
					draw.circle(x / SIDELENGTH, y / SIDELENGTH, 
							0.5 / SIDELENGTH);
				}
				
/*				// Draw a rightwards line
				draw.line(x / SIDELENGTH, y / SIDELENGTH, (x-1) / SIDELENGTH, y / SIDELENGTH);
				// Draw a downwards line
				draw.line(x / SIDELENGTH, y / SIDELENGTH, x / SIDELENGTH, (y-1) / SIDELENGTH);
				*/
			}
		}
		
		draw.show();
	}
	
    public static void main(String[] args) {
    	try {
    		// TODO
    //		new GraphExtractor("/Users/adam/Documents/workspace/independent_set/frb40-19-1.mis");
    //		new GraphExtractor("/Users/adam/Documents/workspace/independent_set/frb35-17-1.clq");
	//		new GraphExtractor("/Users/adam/Documents/workspace/independent_set/frb30-15-1.mis");
    //		new GraphExtractor("/Users/adam/Documents/workspace/independent_set/mygraph.txt");
    		new GraphExtractor("/Users/adam/Documents/workspace/independent_set/4grid.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}

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
	
	private BufferedReader reader;
	
	private HashMap<Integer, Boolean> graph;
	private HashSet<Integer> nodesInGraph;

	private HashMap<Integer, Node> IDsToNodes;
	
	private int vertexCount;
	
	public GraphExtractor(String path) throws IOException {
		
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
		
		List<Integer> finalIncludedList = findIndependentSetTruncation(); // TODO
		
		System.out.println("INCLUDED " + finalIncludedList.size() + " NODES:");
		for (int i=0; i<finalIncludedList.size(); i++) {
			System.out.println(finalIncludedList.get(i));
		} 
		
//		System.out.println(countTriangles());
	}
	
	@SuppressWarnings("unchecked")
	private List<Integer> findIndependentSetTruncation() {
		int depth = 3;
		
		boolean depthTooSmall = false;
		
		List<Integer> listOfIncludedNodeIDs = new ArrayList<Integer>();
		
		// These below are going to be used to check convergence
		HashMap<Integer, Double> idsToBonuses;
		HashMap<Integer, Double> oldIDsToBonuses;
		
		// This is used to cut the converging off if it's taking forever
		int iterationsSoFar;
		int MAX_ITERATIONS = 10;
		
		String MODE = "RANDOM_CHOOSE";
		
		while (!nodesInGraph.isEmpty()) {
			System.out.println("NEXT RUN-THROUGH");
			
			idsToBonuses = new HashMap<Integer, Double>();
			oldIDsToBonuses = new HashMap<Integer, Double>();
			oldIDsToBonuses.put(0, 0.0);
			
			iterationsSoFar = 0;
			
			while (!idsToBonuses.equals(oldIDsToBonuses) && (iterationsSoFar < MAX_ITERATIONS)) {
				/*
				for (int i=0; i<vertexCount; i++) {
					IDsToNodes.get(i).resetBonus();
				} */
				
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
			
			
			// Set of IDs of nodes that are on.
			HashSet<Integer> nodesThatAreOn = new HashSet<Integer>();
			
			// Iterate over every node; take every node that's definitely on, and include 
			// it. Throw its neighbors into the ocean, as an offering to the Drowned God.
			for (int i=0; i<vertexCount; i++) {
				if (graph.get(i)) {
					if (IDsToNodes.get(i).bonusEstimate == 1) {
						nodesThatAreOn.add(i);
					}
				}
			}
			
			if (!nodesInGraph.isEmpty()) {
			
				// If nodesThatAreOn is empty, we have to eliminate one of the nodes in the graph
				// at random, and repeat.
				if (nodesThatAreOn.isEmpty()) {
					delete(randomChoose(nodesInGraph));
				}
				
				// In this case, we have to check that the included nodes make sense. 
				// (In particular, no two included nodes can be adjacent)
				// If they do,
				// we delete each included node and all its neighbors
				
				else {

					depthTooSmall = false;
					
					Iterator<Integer> nodesThatAreOnIterator = nodesThatAreOn.iterator();
					
					// Check every pair of nodes for adjacency.
					// TODO factor of 2 optimization possible
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
									List<Integer> listOfNodesToBeIncluded = findIndependentSetTruncation();
									
									graph = oldGraph;
									nodesInGraph = oldNodesInGraph;
									
									for (int i=0; i<listOfNodesToBeIncluded.size(); i++) {
										include(listOfNodesToBeIncluded.get(i), IDsToNodes, 
												listOfIncludedNodeIDs);
									}
									
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
									
									depthTooSmall = true;
									include(randomChoose(nodesInGraph), IDsToNodes, 
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
					
					
					nodesThatAreOnIterator = nodesThatAreOn.iterator();
					
					while ((nodesThatAreOnIterator.hasNext()) && (!depthTooSmall)) {
						int nextNodeID = nodesThatAreOnIterator.next();
						
						include(nextNodeID, IDsToNodes, listOfIncludedNodeIDs);
					} 
					
//					include(randomChoose(nodesThatAreOn), IDsToNodes, listOfIncludedNodeIDs);
				}
			}
		}
		
		return listOfIncludedNodeIDs;
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
	
	private List<Integer> findIndependentSetGreedy() {
		List<Integer> listOfIncludedNodeIDs = new ArrayList<Integer>();
		
		while (!nodesInGraph.isEmpty()) {
			include(randomChoose(nodesInGraph), IDsToNodes, 
					listOfIncludedNodeIDs);
		}
		
		return listOfIncludedNodeIDs;
	}
	
	private List<Integer> findIndependentSetGreedyWithDegrees() {
		List<Integer> listOfIncludedNodeIDs = new ArrayList<Integer>();
		
		int bestDegree;
		int bestNode;
		
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
		}
		
		return listOfIncludedNodeIDs;
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
		
	public <E> E randomChoose(HashSet<E> set) {
		@SuppressWarnings("unchecked")
		E[] choiceArray = (E[]) set.toArray();
		Random rand = new Random();
		return choiceArray[rand.nextInt(choiceArray.length)];
	}
	
    public static void main(String[] args) {
    	try {
    		// TODO
			new GraphExtractor("/Users/adam/Documents/workspace/independent_set/frb30-15-1.mis");
//    		new GraphExtractor("/Users/adam/Documents/workspace/independent_set/mygraph.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}

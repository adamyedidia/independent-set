package main;

import java.util.*;

public class Node {
	public Set<Integer> neighbors;
	public int id;
	
	public double INITIALIZATION_VALUE = 0.12; // TODO
	public int NUMBER_OF_NODES_IN_START_GRAPH = 50;
	
	public double bonusEstimate = INITIALIZATION_VALUE;
	public double oldBonusEstimate = INITIALIZATION_VALUE;
	
	public Node(HashSet<Integer> neighbors, int id) {
		this.neighbors = neighbors;
		this.id = id;
	}
	
	public int bonus(HashMap<Integer, Node> IDsToNodes, HashMap<Integer, Boolean> graph) { 

		@SuppressWarnings("unchecked")
		HashMap<Integer, Boolean> graphCopy = (HashMap<Integer, Boolean>) graph.clone();
		graphCopy.put(id, false);
		
			int returnValue = 1;
			Iterator<Integer> neighborIterator = neighbors.iterator();
			while (neighborIterator.hasNext()) {
				int nextNeighborID = neighborIterator.next();
				if (graphCopy.get(nextNeighborID)) {
					int bonusValue = IDsToNodes.get(nextNeighborID).bonus(IDsToNodes, 
							graphCopy);
					
					returnValue -= bonusValue;
					
				}	
				graphCopy.put(nextNeighborID, false);
			}
		
		return Math.max(0, returnValue);
	}
	
	public double approximateBonus(HashMap<Integer, Node> IDsToNodes, HashMap<Integer, Boolean> graph,
			int depth) {
		
		if (depth == 0) {
			return oldBonusEstimate;			
//			return bonusEstimate;
		}
		
		else {
			@SuppressWarnings("unchecked")
			HashMap<Integer, Boolean> graphCopy = (HashMap<Integer, Boolean>) graph.clone();
			graphCopy.put(id, false);
			
			double returnValue = 1.0;
			Iterator<Integer> neighborIterator = neighbors.iterator();
			while (neighborIterator.hasNext()) {
								
				int nextNeighborID = neighborIterator.next();
				
				if (graphCopy.get(nextNeighborID)) {
					
					double bonusValue = IDsToNodes.get(nextNeighborID).approximateBonus(IDsToNodes, 
							graphCopy, depth - 1);
			
					returnValue -= bonusValue;
					
				}	
				graphCopy.put(nextNeighborID, false);
			}
			
			return Math.max(0.0, returnValue);
		}
	}
	
	public void resetBonus() {
		oldBonusEstimate = bonusEstimate;
	}
	
	public void reinitializeBonus(HashSet<Integer> nodesInGraph) {
		bonusEstimate = INITIALIZATION_VALUE * NUMBER_OF_NODES_IN_START_GRAPH / nodesInGraph.size();
	}
	
	public void setBonusTo(double value) {
		bonusEstimate = value;
		oldBonusEstimate = value;
	}
	
	public void setBonusLikeAdamWould(HashMap<Integer, Node> IDsToNodes) {
		double multCounter = 1;
		
		for (int nodeID : neighbors) {
			multCounter *= 1 - IDsToNodes.get(nodeID).oldBonusEstimate;
		}
		
		bonusEstimate = multCounter;
	}
	
	public int countLiveNeighbors(HashMap<Integer, Boolean> graph) {
		int neighborCount = 0;
		
		Iterator<Integer> graphIterator = graph.keySet().iterator();
		while (graphIterator.hasNext()) {
			int nextNeighbor = graphIterator.next();
			if (graph.get(nextNeighbor)) {
				neighborCount++;
			}
		}
		
		return neighborCount;
	}
	
    public static void main(String[] args) {
    	
    	HashSet<Integer> node1neighbors = new HashSet<Integer>();
    	node1neighbors.add(2);
    	node1neighbors.add(3);
    	
    	HashSet<Integer> node2neighbors = new HashSet<Integer>();
    	node2neighbors.add(1);
    	node2neighbors.add(3);
    	node2neighbors.add(4);
    	
    	HashSet<Integer> node3neighbors = new HashSet<Integer>();
    	node3neighbors.add(1);
    	node3neighbors.add(2);
    	node3neighbors.add(4);
    	
    	HashSet<Integer> node4neighbors = new HashSet<Integer>();
    	node4neighbors.add(2);
    	node4neighbors.add(3);
    	
    	Node node1 = new Node(node1neighbors, 1);
    	Node node2 = new Node(node2neighbors, 2);
    	Node node3 = new Node(node3neighbors, 3);
    	Node node4 = new Node(node4neighbors, 4);
    	
    	HashMap<Integer, Node> IDsToNodes = new HashMap<Integer, Node>();
    	
    	IDsToNodes.put(1, node1);
    	IDsToNodes.put(2, node2);
    	IDsToNodes.put(3, node3);
    	IDsToNodes.put(4, node4);
    	
    	HashMap<Integer, Boolean> graph = new HashMap<Integer, Boolean>();
    	
    	graph.put(1, true);
    	graph.put(2, true);
    	graph.put(3, true);
    	graph.put(4, true);
    }
}

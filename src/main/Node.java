package main;

import java.util.*;

public class Node {
	public Set<Integer> neighbors;
	public int id;
	
	public int bonusEstimate = 0;
	
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
	
	public int approximateBonus(HashMap<Integer, Node> IDsToNodes, HashMap<Integer, Boolean> graph,
			int depth) {
		
		if (depth == 0) {
			return bonusEstimate;
		}
		
		else {
			@SuppressWarnings("unchecked")
			HashMap<Integer, Boolean> graphCopy = (HashMap<Integer, Boolean>) graph.clone();
			graphCopy.put(id, false);
			
			int returnValue = 1;
			Iterator<Integer> neighborIterator = neighbors.iterator();
			while (neighborIterator.hasNext()) {
								
				int nextNeighborID = neighborIterator.next();
				
				if (graphCopy.get(nextNeighborID)) {
					
/*					if ((!graph.containsValue(false)) && (id == 9) && (nextNeighborID == 3)) {
						Iterator<Integer> iter = graphCopy.keySet().iterator();
						while (iter.hasNext()) {
							int ID = iter.next();
							System.out.println("" + ID + " " + graphCopy.get(ID));
						}
					} */
					
					int bonusValue = IDsToNodes.get(nextNeighborID).approximateBonus(IDsToNodes, 
							graphCopy, depth - 1);
					
/*					if ((graphCopy.get(0)) && 
							(!graphCopy.get(1)) &&
							(graphCopy.get(2)) && 
							(!graphCopy.get(3)) && 
							(!graphCopy.get(4)) && 
							(!graphCopy.get(5)) && 
							(graphCopy.get(6)) && 
							(graphCopy.get(7)) && 
							(graphCopy.get(8)) && 
							(!graphCopy.get(9)) && 
							(id == 4) &&
							(nextNeighborID == 7)) {
						System.out.println("" + bonusValue + " " + depth);
					} */
					
					returnValue -= bonusValue;
					
				}	
				graphCopy.put(nextNeighborID, false);
			}
			
			return Math.max(0, returnValue);
		}
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

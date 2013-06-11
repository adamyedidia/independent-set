package main;

import java.util.HashMap;

public class HashMapWithSmartHashCode {
	private HashMap<Integer, Boolean> nodesAreIncluded;
	private int bonusTarget;
	
	public HashMapWithSmartHashCode(HashMap<Integer, Boolean> nodesAreIncluded,
			int bonusTarget) {
		this.nodesAreIncluded = nodesAreIncluded;
		this.bonusTarget = bonusTarget;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HashMapWithSmartHashCode other = (HashMapWithSmartHashCode) obj;
		if (bonusTarget != other.bonusTarget)
			return false;
		if (nodesAreIncluded == null) {
			if (other.nodesAreIncluded != null)
				return false;
		} else {
			for (int i=0; i<nodesAreIncluded.size(); i++) {
				if (nodesAreIncluded.get(i) ^ ((HashMapWithSmartHashCode) obj).getNodeBoolean(i)) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + bonusTarget;
		
		for (int i=0; i<nodesAreIncluded.size(); i++) {
			if (nodesAreIncluded.get(i)) {
				result = prime * result + 1;
			}
			else {
				result = prime * result;
			}
		}

		return result;
	}
	
	public boolean getNodeBoolean(int index) {
		return nodesAreIncluded.get(index);
	}
}

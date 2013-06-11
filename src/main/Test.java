package main;

import java.util.*;

public class Test {
	public Test() {
		HashSet<HashMapWithSmartHashCode> set = new HashSet<HashMapWithSmartHashCode>();
		
		HashMap<Integer, Boolean> map1 = new HashMap<Integer, Boolean>();
		map1.put(0, true);
		map1.put(1, false);
		map1.put(2, true);
		
		set.add(new HashMapWithSmartHashCode(map1, 1));
		
		HashMap<Integer, Boolean> map2 = new HashMap<Integer, Boolean>();
		map2.put(0, true);
		map2.put(1, false);
		map2.put(2, false);
		
		System.out.println(set.contains(new HashMapWithSmartHashCode(map2, 1)));
	}
	
    public static void main(String[] args) {
    	new Test();
    }
}

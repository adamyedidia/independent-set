package main;

import java.util.*;

public class Test {
	public Test() {
		Draw draw = new Draw();
		draw.filledCircle(0.5, 0.5, 0.1);
		draw.show(1000);
		draw.clear();
		draw.circle(0.5, 0.5, 0.1);
		draw.show();
	}
	
    public static void main(String[] args) {
    	new Test();
    }
}

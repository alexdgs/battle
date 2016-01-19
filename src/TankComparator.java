import java.util.Comparator;

/* This comparator gives a greater value to those elements
 * wanted to select. In a Priority Queue, the desired elements
 * (the farthest, closest, etc.) are send at the end of the Queue
 * while the undesirable stay in the head. So, as the Queue grows
 * and if its size is greater than the specified, the head element is
 * removed via a poll() call.*/

public class TankComparator implements Comparator<Tank> {
	
	static final int CLOSEST = 1;
	static final int FARTHEST = 2;
	static final int STRONGEST = 3;
	static final int WEAKEST = 4;
	
	int op;
	
	public TankComparator(int op) {
		this.op = op;
	}
	
	public int compare(Tank t1, Tank t2) {
		if(op == CLOSEST) {
			if(t1.distTotarget > t2.distTotarget) return -1;
			else return 1;
		} else if(op == FARTHEST){
			if(t1.distTotarget < t2.distTotarget) return -1;
			else return 1;
		} else if(op == STRONGEST){
			if(t1.hitPoints < t2.hitPoints) return -1;
			else return 1;
		} else {
			if(t1.hitPoints > t2.hitPoints) return -1;
			else return 1;
		}
	}
}

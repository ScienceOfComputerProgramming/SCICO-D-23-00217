package repairer;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class TreeSetDeadlock extends Thread {

    Set s1, s2;

    public TreeSetDeadlock(Set s1, Set s2) {
        this.s1 = s1;
        this.s2 = s2;
    }

    public void run() {
        s1.addAll(s2);
}

public class TreeSetDead {
    Set l1 = Collections.synchronizedSet(new TreeSet());
    Set l2 = Collections.synchronizedSet(new TreeSet());

     public  void main(String[] args) {

         (new TreeSetDeadlock(l1, l2)).start();
         (new TreeSetDeadlock(l2, l1)).start();
    }
    }
}
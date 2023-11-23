package repairer;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class LinkedSetDeadlock extends Thread {

    Set s1, s2;

    public LinkedSetDeadlock(Set s1, Set s2) {
        this.s1 = s1;
        this.s2 = s2;
    }

    public void run() {
        s1.addAll(s2);
}

public class LinkedSetDead {
    Set l1 = Collections.synchronizedSet(new LinkedHashSet());
    Set l2 = Collections.synchronizedSet(new LinkedHashSet());

     public  void main(String[] args) {

         (new LinkedSetDeadlock(l1, l2)).start();
         (new LinkedSetDeadlock(l2, l1)).start();
    }
    }
}
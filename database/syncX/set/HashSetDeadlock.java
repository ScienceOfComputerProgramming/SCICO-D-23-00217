package repairer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class HashSetDeadlock extends Thread {

    Set s1, s2;

    public HashSetDeadlock(Set s1, Set s2) {
        this.s1 = s1;
        this.s2 = s2;
    }

    public void run() {
        s1.addAll(s2);
}

public class HashSetDead {
    Set l1 = Collections.synchronizedSet(new HashSet());
    Set l2 = Collections.synchronizedSet(new HashSet());

     public  void main(String[] args) {

         (new HashSetDeadlock(l1, l2)).start();
         (new HashSetDeadlock(l2, l1)).start();
    }
    }
}
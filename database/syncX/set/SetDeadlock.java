package repairer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SetDeadlock extends Thread {
    
    Set s1, s2;

    public SetDeadlock(Set s1, Set s2) {
        this.s1 = s1;
        this.s2 = s2;
    }

    public void run() {
        s1.addAll(s2);
}

public class SetDead {
    Set l1 = Collections.synchronizedSet(new HashSet());
    Set l2 = Collections.synchronizedSet(new HashSet());

     public  void main(String[] args) {

         (new SetDeadlock(l1, l2)).start();
         (new SetDeadlock(l2, l1)).start();
    }
    }
}
package repairer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class IdentityHashMapDeadlock extends Thread {
    Map l1 ,l2;
    public IdentityHashMapDeadlock(Map l1,Map l2){
        this.l1 = l1;
        this.l2 = l2;
    }
    public void run(){
        l1.equals(l2);
    }
    public class IdentityHashMapDead{

        Map l1 = Collections.synchronizedMap(new HashMap<>());
        Map l2 = Collections.synchronizedMap(new HashMap<>());


        public void main(String[] args) {
            Thread t1 = (new IdentityHashMapDeadlock(l1, l2));
            Thread t2 = (new IdentityHashMapDeadlock(l2, l1));
            t1.start();
            t2.start();
        }
    }
}

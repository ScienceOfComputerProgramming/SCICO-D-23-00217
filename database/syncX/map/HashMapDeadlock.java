package repairer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HashMapDeadlock extends Thread{
    Map l1 ,l2;
    public HashMapDeadlock(Map l1,Map l2){
        this.l1 = l1;
        this.l2 = l2;
    }
    public void run(){
        l1.equals(l2);
    }
    public class HashMapDead{

        Map l1 = Collections.synchronizedMap(new HashMap<>());
        Map l2 = Collections.synchronizedMap(new HashMap<>());


        public void main(String[] args) {
            Thread t1 = (new HashMapDeadlock(l1, l2));
            Thread t2 = (new HashMapDeadlock(l2, l1));
            t1.start();
            t2.start();
        }
    }
}

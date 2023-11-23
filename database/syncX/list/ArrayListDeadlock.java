package repairer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ArrayListDeadlock extends Thread{
    List v1, v2;
    public ArrayListDeadlock(List v1, List v2) {
        this.v1 = v1;
        this.v2 = v2;
    }
    public void run() {
        v1.addAll(v2);
    }
    public class ArrayListDead{

        List l1 = Collections.synchronizedList(new LinkedList());
        List l2 = Collections.synchronizedList(new LinkedList());

        public void main(String[] args){
            (new ArrayListDeadlock(l1, l2)).start();
            (new ArrayListDeadlock(l2, l1)).start();
        }

    }
}

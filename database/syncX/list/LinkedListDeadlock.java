package repairer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class LinkedListDeadlock extends Thread{
    List v1, v2;
    public LinkedListDeadlock(List v1, List v2) {
        this.v1 = v1;
        this.v2 = v2;
    }
    public void run() {
        v1.addAll(v2);
    }
}

class LinkedListDead{

    List l1 = Collections.synchronizedList(new LinkedList());
    List l2 = Collections.synchronizedList(new LinkedList());

    public void main(String[] args){
        (new LinkedListDeadlock(l1, l2)).start();
        (new LinkedListDeadlock(l2, l1)).start();
    }

}

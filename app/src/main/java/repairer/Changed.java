package repairer;
import java.util.concurrent.locks.ReentrantLock;

public class Changed {
    private ReentrantLock lock = new ReentrantLock();

    public void concurrent(){
        lock.lock();
        lock.unlock();
    }

    public void TryFinally(){
        lock.lock();
        try {
            System.out.println("ok");
        } finally {
            lock.unlock();
        }
    }

    public void TryFinally2(){
        try {
            lock.lock();
            System.out.println("ok");
        } finally {
        }
    }

    public void Forloop(){
        for(int i=0;i<100;i++){
            lock.lock();
            try {
                System.out.println("ok");
            } finally {
            }
        }
    }

    public void IfElse(){
        int i =0;
        try {
            lock.lock();
            if (i>0) {
                System.out.println("yes");
                if (i!=0) {
                    lock.lock();
                    System.out.println("ha");
                    lock.unlock();
                }
            }else {
            }
        } finally {
            if(i<0){
            }
        }
    }

    public void IfElse2(){
        int i =0;
        lock.lock();
        if (i<0) {
            try {
                if (i > 0) {
                    System.out.println("yes");
                    if (i == 0) {
                        //if we timed out, or got interrupted
                        // remove ourselves from the waitlist
                        lock.lock();
                        try {
                            System.out.println("yes");
                        } finally {
                            lock.unlock();
                        }
                    }
                } else {
                }
            } finally {
                if(i>10){
                }
            }
        }
        lock.unlock();
    }
}
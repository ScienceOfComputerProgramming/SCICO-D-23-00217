package repairer;

import java.util.concurrent.CyclicBarrier;

class DeadlockRunnable extends Thread {
    private final Object lockToGrabFirst;
    private final Object lockToGrabSecond;
    private final CyclicBarrier ensureBothThreadsGrabTheirLock;

    public DeadlockRunnable(CyclicBarrier ensureBothThreadsGrabTheirLock,
                            Object lockToGrabFirst,
                            Object lockToGrabSecond) {
        this.ensureBothThreadsGrabTheirLock = ensureBothThreadsGrabTheirLock;
        this.lockToGrabFirst = lockToGrabFirst;
        this.lockToGrabSecond = lockToGrabSecond;
    }

    @Override
    public void run() {
//        synchronized (this){
            synchronized (lockToGrabFirst) {
                synchronized (lockToGrabSecond) {
                    System.out.println(lockToGrabFirst);
                }
            }
//        }
    }
}
public class SimpleMonitorDeadlockMain {

    public static void main(String[] args) throws InterruptedException {
        Object lock1 = new Object();
        Object lock2 = new Object();
        CyclicBarrier ensureBothThreadsGrabTheirLock = new CyclicBarrier(2);

        Thread thread1 = new DeadlockRunnable(ensureBothThreadsGrabTheirLock, lock1, lock2);
        Thread thread2 = new DeadlockRunnable(ensureBothThreadsGrabTheirLock, lock2, lock1);
        thread1.start();
        thread2.start();

        System.out.print("Waiting for thread1 to complete.");
        thread1.join();
        System.out.print("Waiting for thread2 to complete.");
        thread2.join();
        System.out.print("Done!");
    }
}
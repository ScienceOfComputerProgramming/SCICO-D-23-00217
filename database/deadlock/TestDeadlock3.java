package repairer;

class ThreadA1 extends Thread {
    Object l1;
    Object l2;
    Object l3;
    Object l4;

    public ThreadA1(Object o1, Object o2, Object o3, Object o4) {
        l1 = o1;
        l2 = o2;
        l3 = o3;
        l4 = o4;
    }
    public void run() {
        synchronized (l1) {
            synchronized (l2) {
                System.out.println(l2);
            }
            synchronized (l2) {
                System.out.println(l2);
            }
        }
        try {
            sleep(5);
        }
        catch (Exception e) {
            System.err.println("Error while sleeping in Thread1");
        }
        synchronized (l3) {
            synchronized (l4) {
                System.out.println(l4);
            }
       }
    }
}

class ThreadA2 extends Thread {
    Object l2;
    Object l3;

    public ThreadA2(Object o2, Object o3) {
        l2 = o2;
        l3 = o3;
    }

    public void run() {
        synchronized (l2) {
            synchronized (l3) {
                System.out.println(l3);
            }
        }
    }
}

public class TestDeadlock3 {

    static Object o1 = new Object();
    static Object o2 = new Object();
    static Object o3 = new Object();
    static Object o4 = new Object();


    public static void main(String[] args) {
        Thread t1 = new ThreadA1(o1, o2, o3, o4);
        Thread t2 = new ThreadA2(o4, o3);

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        }
        catch (Exception e) {
            System.err.println("Exception occurred while waiting for threads " + e.toString());
        }
    }

}


package repairer;


public class TestDeadLock {
    public static void main(String[] args) {
        String c1 = "1";
        String c2 = "1";
        String c3 = "1";
        String c4 = "1";
        String c5 = "1";
        new Philosopher("苏格拉底", c1, c2).start();
        new Philosopher("柏拉图", c2, c3).start();
        new Philosopher("亚里士多德", c3, c4).start();
        new Philosopher("赫拉克利特", c4, c5).start();
        new Philosopher("阿基米德", c5, c1).start();

    }
}

class Philosopher extends Thread {
    String left;
    String right;

    public Philosopher(String name, String left, String right) {
        super(name);
        this.left = left;
        this.right = right;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                synchronized (this) {
                    synchronized (this) {
                        synchronized (this) {
                            synchronized (left) {
                                synchronized (right) {
                                    eat();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    private void eat() {
        System.out.println("eat");
    }
}

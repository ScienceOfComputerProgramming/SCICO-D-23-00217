package repairer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

// 模拟 SocketClientFactory 类
class SocketClientFactory {
    // 定义一些属性
    int bindCount = 0;
    private int idleCount = 0;
    private int freeCount = 0;
    private int usedCount = 0;
    private int deadCount = 0;

    private List<SocketClient> clients;

    // 获取一个线程来运行 SocketClient
    public Thread getThread(SocketClient client) {
        return new Thread(client);
    }

    // 更新使用状态
        public void notifyUse(SocketClient client) {
            synchronized (this) {
            usedCount++;
            freeCount--;
            idleCount--;
            if (client.done) {
                deadCount++;
            }
        }
    }

            // 更新空闲状态
protected void notifyFree(SocketClient client) {
        synchronized (this) {
            idleCount++;
            freeCount++;
            usedCount--;
            if (client.done) {
                deadCount--;
            }
        }
    }

            // 关闭所有的 SocketClient
    public void shutdown() {
        synchronized (this) {
            for (SocketClient client : clients) {
                if (client != null && client.alive) {
                    client.kill(true);
                }
            }
        }
    }
}

// 模拟 SocketClient 类
class SocketClient implements Runnable {

    private Socket socket; // 当前处理的 socket
    private InputStream in; // socket 的输入流
    private OutputStream out; // socket 的输出流
    boolean alive; // 是否存活
    boolean done; // 是否完成
    private Thread thread; // 当前运行的线程
    private SocketClientFactory factory; // 所属的工厂对象

    public SocketClient(SocketClientFactory factory) {
        this.factory = factory;
    }

    // 绑定一个 socket 并启动线程
    public void bind(Socket socket) {
        try {
            this.socket = socket;
            this.in = socket.getInputStream();
            this.out = socket.getOutputStream();
// 更新状态
        synchronized (factory) { // use factory as the lock object**
                factory.notifyUse(this);
                factory.bindCount++;
                alive = true;
                done = false;
        }
// 启动线程
            thread = factory.getThread(this);
            thread.start();
        } catch (IOException ex) {
// 关闭 socket
            try {
                socket.close();
            } catch (Exception e) {}
        }
    }

    // 解绑一个 socket 并关闭资源
    public void unbind() {
// 关闭 socket
        try {
            in.close();
        } catch (Exception ex) {}
        try {
            out.close();
        } catch (Exception ex) {}
        try {
            socket.close();
        } catch (Exception ex) {}
// 更新状态
      synchronized (factory) { // use factory as the lock object**
            factory.notifyFree(this);
            alive = false;
            done = true;
        }
    }

    // 杀死一个 SocketClient
    public void kill(boolean now) {
        if (!alive)
            return;
        if (now) {
// We are shutting down the server, kill the client right now:
            try {
                socket.close();
            } catch (Exception ex) {}
            alive = false;
            done = true;
        } else {
// We are just killing one client, be nice:
            interruptConnection();
        }
    }

    // 模拟处理请求的方法，可能抛出异常或关闭连接
    public void processRequest(Request request) throws Exception {
// do something with the request, such as sending a response or forwarding it to another server
// ...
// randomly throw an exception or close the connection to simulate an error or a non-persistent connection
        double r = Math.random();
        if (r < 0.1) {
            throw new Exception("Something went wrong");
        } else if (r < 0.2) {
            socket.close();
        }
    }

    // 模拟获取下一个请求的方法，可能返回 null 表示连接已关闭或超时
    public Request getNextRequest() throws IOException {
// read from the input stream and parse the request, such as HTTP or FTP protocol
// ...
// randomly return null to simulate a closed or timed out connection
        double r = Math.random();
        if (r < 0.1) {
            return null;
        } else {
            return new Request(); // a dummy request object
        }
    }

    // 模拟判断是否保持连接的方法，可能返回 false 表示不需要保持连接或连接已断开
    public boolean tryKeepConnection() throws IOException {
// check some conditions, such as the request header or the socket status
// ...
// randomly return false to simulate a non-persistent connection or a broken connection
        double r = Math.random();
        if (r < 0.1) {
            return false;
        } else {
            return true;
        }
    }

    // 模拟处理异常的方法，可能关闭连接或中断连接
    public void error(Exception ex) {
// log the exception and send an error response to the client
// ...
// randomly close the connection or interrupt the connection to simulate different ways of handling errors
        double r = Math.random();
        if (r < 0.5) {
            try {
                socket.close();
            } catch (Exception e) {}
        } else {
            interruptConnection();
        }
    }

    // 模拟中断连接的方法，不关闭 socket，但设置 alive 为 false
    public void interruptConnection() {
        alive = false;
    }

    @Override
    public void run() {

    }
}

// 模拟请求对象，没有实际功能
class Request {
// some fields and methods
}

// 模拟两个线程分别调用 shutdown 和 bind 的情况
public class DeadlockDemo {
    public static void main(String[] args) {
// 创建一个 SocketClientFactory 对象
        SocketClientFactory factory = new SocketClientFactory();

// 创建一个线程调用 shutdown 方法
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                factory.shutdown();
            }
        });

// 创建另一个线程调用 bind 方法
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
// 创建一个 Socket 对象
                    Socket socket = new Socket("localhost", 8080);
// 创建一个 SocketClient 对象
                    SocketClient client = new SocketClient(factory);
// 调用 bind 方法
                    client.bind(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

// 启动两个线程
        t1.start();
        t2.start();
    }
}

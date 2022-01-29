package me.aiot;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public final class SocketServer {
    public static final int PORT = 14213;

    public HashMap<SocketAddress, Socket> availableSocketClients = new HashMap<>();

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    private boolean running = true;

    public void init() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            while (running) {
                // 一旦有堵塞, 则表示服务器与客户端获得了连接
                var client = serverSocket.accept();
                client.setKeepAlive(true);
                System.out.println(client.getInetAddress().toString() + " 连接");
                // 处理这次连接
                CompletableFuture.runAsync(new HandlerThread(this, client));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class HandlerThread implements Runnable {
        private final SocketServer server;
        private Socket socket;

        public HandlerThread(SocketServer server, Socket socket) {
            this.server = server;
            this.socket = socket;
            server.availableSocketClients.put(socket.getRemoteSocketAddress(), socket);
        }

        public void run() {
            try {
                // 读取客户端数据
                var dataInputStream = new DataInputStream(socket.getInputStream());
                var dataOutputStream = new DataOutputStream(socket.getOutputStream());
                var reader = new BufferedReader(new InputStreamReader(dataInputStream));
                while (true) {
                    var clientInputStr = reader.readLine();//这里要注意和客户端输出流的写方法对应,否则会抛 EOFException
                    if (clientInputStr != null) {
                        // 处理客户端数据
                        System.out.println("客户端发过来的内容:" + clientInputStr);
                        if ("closeServer".equals(clientInputStr)) {
                            break;
                        }
                    }
                }
                dataOutputStream.close();
                dataInputStream.close();
                socket.close();
                server.availableSocketClients.remove(socket.getRemoteSocketAddress());
                socket = null;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                        server.availableSocketClients.remove(socket.getRemoteSocketAddress());
                    } catch (Exception e) {
                        socket = null;
                        System.out.println("服务端 finally 异常:" + e.getMessage());
                    }
                }
            }
        }
    }
}

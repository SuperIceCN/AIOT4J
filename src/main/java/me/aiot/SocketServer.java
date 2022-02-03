package me.aiot;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public final class SocketServer {
    public static final int PORT = 14213;

    public HashMap<SocketAddress, AIOTClient> availableSocketClients = new HashMap<>();

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    private boolean running = true;

    public AIOTClient getClient() {
        if (availableSocketClients.size() > 0) {
            return availableSocketClients.values().stream().findFirst().get();
        } else {
            return null;
        }
    }

    public void init() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            while (running) {
                // 一旦有堵塞, 则表示服务器与客户端获得了连接
                var client = serverSocket.accept();
                client.setKeepAlive(true);
                System.out.println(client.getInetAddress().toString() + " 连接");
                // 处理这次连接
                CompletableFuture.runAsync(new AIOTClient(this, client));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init(ServerHandler serverHandler, ClientHandler handler) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            while (running) {
                // 一旦有堵塞, 则表示服务器与客户端获得了连接
                var client = serverSocket.accept();
                client.setKeepAlive(true);
                System.out.println(client.getInetAddress().toString() + " 连接");
                // 处理这次连接
                CompletableFuture.runAsync(new AIOTClient(this, client, handler));
                serverHandler.handle(client);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        availableSocketClients.values().forEach(aiotClient -> {
            if (aiotClient.dataOutputStream == null) {
                try {
                    aiotClient.dataOutputStream = new DataOutputStream(aiotClient.socket.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                aiotClient.dataOutputStream.writeBytes("closeClient\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static class AIOTClient implements Runnable {
        private final SocketServer server;
        private Socket socket;
        private volatile ClientHandler handler;
        private DataOutputStream dataOutputStream = null;
        private Promise<String> promise;

        public AIOTClient(SocketServer server, Socket socket, ClientHandler clientHandler) {
            this.server = server;
            this.socket = socket;
            this.handler = clientHandler;
            server.availableSocketClients.put(socket.getRemoteSocketAddress(), this);
        }

        public AIOTClient(SocketServer server, Socket socket) {
            this(server, socket, (socket1, message) -> System.out.println("客户端发过来的内容:" + message));
        }

        public ClientHandler getHandler() {
            return handler;
        }

        public void setHandler(ClientHandler clientHandler) {
            this.handler = clientHandler;
        }

        public Socket getSocket() {
            return socket;
        }

        public Promise<String> send(String str) {
            if (dataOutputStream == null) {
                try {
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dataOutputStream != null) {
                try {
                    System.err.println(str + "\n");
                    dataOutputStream.writeBytes(str + "\n");
                    this.promise = new Promise<>();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return promise;
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
                        this.handler.handle(this.socket, clientInputStr);
                        if (promise != null) {
                            promise.resolve(clientInputStr);
                            promise = null;
                        }
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

    public interface ServerHandler {
        void handle(Socket socket);
    }

    public interface ClientHandler {
        void handle(Socket socket, String message);
    }
}

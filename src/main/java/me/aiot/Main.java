package me.aiot;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Scanner;

public final class Main {
    public static String message = null;
    public static void main(String... args) {
        final var socketServer = new SocketServer();
        final var socketThread = new Thread(socketServer::init);
        socketThread.start();
        final var scanner = new Scanner(System.in, "GBK");
        while (scanner.hasNextLine()) {
            var cmd = scanner.nextLine().replace("\n", "");
            if("stop".equals(cmd)){
                socketServer.setRunning(false);
                socketThread.interrupt();
                System.exit(0);
            }else {
                socketServer.availableSocketClients.values().forEach(socket -> {
                    try {
                        new DataOutputStream(socket.getOutputStream()).writeBytes(cmd + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }
}

package me.aiot.js;

import com.caoccao.javet.annotations.V8Function;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.NodeRuntime;
import com.caoccao.javet.values.reference.V8ValueFunction;
import com.caoccao.javet.values.reference.V8ValuePromise;
import me.aiot.SocketServer;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicIntegerArray;

public final class API {
    private final SocketServer socketServer;
    private final NodeRuntime nodeRuntime;

    public API(SocketServer socketServer, NodeRuntime nodeRuntime) {
        this.socketServer = socketServer;
        this.nodeRuntime = nodeRuntime;
    }

    @V8Function
    public boolean isConnected() {
        return socketServer.getClient() != null;
    }

    @V8Function
    public void startCollectData() {
        final var client = socketServer.getClient();
        if (client != null)
            client.startCollect();
    }

    @V8Function
    public int readTemperature() throws JavetException {
        final var client = socketServer.getClient();
        if (client != null)
            return client.data.temperature;
        return -1;
    }

    @V8Function
    public int readHumidity() throws JavetException {
        final var client = socketServer.getClient();
        if (client != null)
            return client.data.humidity;
        return -1;
    }

    @V8Function
    public int readLight() throws JavetException {
        final var client = socketServer.getClient();
        if (client != null)
            return client.data.light;
        return -1;
    }

    @V8Function
    public void print(String str) {
        System.out.println(str);
    }
}

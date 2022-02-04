package me.aiot.js;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.NodeRuntime;
import com.caoccao.javet.interop.V8Host;
import com.caoccao.javet.interop.converters.JavetProxyConverter;
import me.aiot.SocketServer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class JSPool {
    public final static JSPool POOL = new JSPool();

    public final Map<String, NodeRuntime> runtimeMap = new LinkedHashMap<>();
    public final Map<String, String> codeMap = new HashMap<>();

    public void addJSModule(SocketServer server, String name, String code) {
        try {
            final NodeRuntime rt = V8Host.getNodeInstance().createV8Runtime();
            final var javetProxyConverter = new JavetProxyConverter();
            rt.setConverter(javetProxyConverter);
            runtimeMap.put(name, rt);
            codeMap.put(name, code);
            rt.getGlobalObject().set("aiot", new API(server, rt));
        } catch (JavetException e) {
            e.printStackTrace();
        }
    }

    public void runJSModule(String name) {
        CompletableFuture.runAsync(() -> {
            try {
                runtimeMap.get(name).getExecutor(codeMap.get(name)).execute();
                runtimeMap.get(name).await();
            } catch (JavetException e) {
                e.printStackTrace();
            }
        });
    }

    public void saveJSModule(String name, String code) {
        codeMap.put(name, code);
    }
}

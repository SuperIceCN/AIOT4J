package me.aiot;

import java.util.function.Consumer;

public final class Promise<T> {
    private Consumer<T> then = null;

    public void then(Consumer<T> then) {
        this.then = then;
    }

    public void resolve(T t) {
        then.accept(t);
    }
}

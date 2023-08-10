package io.github.egd.prodigal.dynamic.rabbit.router.in;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class DynamicRabbitRouterInAsyncHolder {

    private String exchange;

    private String routingKey;

    private byte[] bytes;

    private final CompletableFuture<Object> future;

    public DynamicRabbitRouterInAsyncHolder() {
        this.future = new CompletableFuture<>();
    }

    public DynamicRabbitRouterInAsyncHolder(String exchange, String routingKey, byte[] bytes) {
        this.exchange = exchange;
        this.routingKey = routingKey;
        if (bytes == null) {
            throw new IllegalArgumentException("bytes is null");
        }
        this.bytes = bytes;
        this.future = new CompletableFuture<>();
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getExchange() {
        return exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void complete(Object value) {
        this.future.complete(value);
    }

    public int byteLength() {
        return this.bytes.length;
    }

    public Object get() throws ExecutionException, InterruptedException {
        return this.future.get();
    }
}

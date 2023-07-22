package io.github.egd.prodigal.dynamic.rabbit.listener;

import java.util.List;

public class DynamicRabbitListenerFactoryProperties {

    private String id;

    private String group;

    private List<String> queueNames;

    private int concurrentConsumers = 1;

    private int maxConcurrentConsumers = 4;

    private int prefetchCount = 400;

    private int batchSize = 100;

    private boolean batchListener = true;

    private boolean consumerBatchEnabled = true;

    private long receiveTimeout = 15000L;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public List<String> getQueueNames() {
        return queueNames;
    }

    public void setQueueNames(List<String> queueNames) {
        this.queueNames = queueNames;
    }

    public int getConcurrentConsumers() {
        return concurrentConsumers;
    }

    public void setConcurrentConsumers(int concurrentConsumers) {
        this.concurrentConsumers = concurrentConsumers;
    }

    public int getMaxConcurrentConsumers() {
        return maxConcurrentConsumers;
    }

    public void setMaxConcurrentConsumers(int maxConcurrentConsumers) {
        this.maxConcurrentConsumers = maxConcurrentConsumers;
    }

    public int getPrefetchCount() {
        return prefetchCount;
    }

    public void setPrefetchCount(int prefetchCount) {
        this.prefetchCount = prefetchCount;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public boolean isBatchListener() {
        return batchListener;
    }

    public void setBatchListener(boolean batchListener) {
        this.batchListener = batchListener;
    }

    public boolean isConsumerBatchEnabled() {
        return consumerBatchEnabled;
    }

    public void setConsumerBatchEnabled(boolean consumerBatchEnabled) {
        this.consumerBatchEnabled = consumerBatchEnabled;
    }

    public long getReceiveTimeout() {
        return receiveTimeout;
    }

    public void setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }
}

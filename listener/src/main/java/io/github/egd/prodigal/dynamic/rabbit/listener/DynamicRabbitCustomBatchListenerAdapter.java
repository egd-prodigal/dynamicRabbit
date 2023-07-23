package io.github.egd.prodigal.dynamic.rabbit.listener;

import org.springframework.amqp.core.MessageProperties;

import java.util.List;

public abstract class DynamicRabbitCustomBatchListenerAdapter implements DynamicRabbitCustomBatchListener {

    protected abstract List<String> supportedQueueNames();

    @Override
    public boolean canConsumeQueue(String queueName) {
        List<String> supportedQueueNames = supportedQueueNames();
        return supportedQueueNames != null && supportedQueueNames.contains(queueName);
    }

    @Override
    public boolean supportMessageProperties(MessageProperties messageProperties) {
        return true;
    }

}

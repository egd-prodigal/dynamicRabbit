package io.github.egd.prodigal.dynamic.rabbit.listener;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.batch.BatchingStrategy;
import org.springframework.amqp.rabbit.listener.adapter.BatchMessagingMessageListenerAdapter;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.messaging.support.GenericMessage;

import java.lang.reflect.Method;
import java.util.List;

public class DynamicRabbitBatchMessagingListener extends BatchMessagingMessageListenerAdapter {

    private final DynamicRabbitListenerConfigurer dynamicRabbitListenerConfigurer;

    public DynamicRabbitBatchMessagingListener(DynamicRabbitListenerConfigurer bean, Method method, boolean returnExceptions, RabbitListenerErrorHandler errorHandler, BatchingStrategy batchingStrategy) {
        super(bean, method, returnExceptions, errorHandler, batchingStrategy);
        this.dynamicRabbitListenerConfigurer = bean;
    }

    @Override
    public void onMessageBatch(List<Message> messages, Channel channel) {
        dynamicRabbitListenerConfigurer.consume(new GenericMessage<>(messages), channel);
    }

}

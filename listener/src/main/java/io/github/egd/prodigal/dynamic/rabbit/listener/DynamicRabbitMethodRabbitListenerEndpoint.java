package io.github.egd.prodigal.dynamic.rabbit.listener;

import org.springframework.amqp.rabbit.listener.MethodRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter;

public class DynamicRabbitMethodRabbitListenerEndpoint extends MethodRabbitListenerEndpoint {

    @Override
    protected MessagingMessageListenerAdapter createMessageListenerInstance() {
        return new DynamicRabbitBatchMessagingListener((DynamicRabbitListenerConfigurer) getBean(), getMethod(), true, null, getBatchingStrategy());
    }

}

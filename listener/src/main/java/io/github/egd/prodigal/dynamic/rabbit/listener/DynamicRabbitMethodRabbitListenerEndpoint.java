package io.github.egd.prodigal.dynamic.rabbit.listener;

import org.springframework.amqp.rabbit.listener.MethodRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Map;

public class DynamicRabbitMethodRabbitListenerEndpoint extends MethodRabbitListenerEndpoint {

    private final ApplicationContext applicationContext;

    private DynamicRabbitBatchMessagingListener dynamicRabbitBatchMessagingListener;

    public DynamicRabbitMethodRabbitListenerEndpoint(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    protected MessagingMessageListenerAdapter createMessageListenerInstance() {
        if (this.dynamicRabbitBatchMessagingListener == null) {
            Map<String, DynamicRabbitCustomBatchListener> dynamicRabbitCustomBatchListenerMap = applicationContext.getBeansOfType(DynamicRabbitCustomBatchListener.class);
            ArrayList<DynamicRabbitCustomBatchListener> dynamicRabbitCustomBatchListeners = dynamicRabbitCustomBatchListenerMap.isEmpty()
                    ? new ArrayList<>() : new ArrayList<>(dynamicRabbitCustomBatchListenerMap.values());
            dynamicRabbitBatchMessagingListener = applicationContext.getBean(DynamicRabbitBatchMessagingListener.class,
                    getId(), getGroup(), getQueueNames(), getBean(), getMethod(), getBatchingStrategy(),
                    dynamicRabbitCustomBatchListeners);
        }
        return this.dynamicRabbitBatchMessagingListener;
    }

}

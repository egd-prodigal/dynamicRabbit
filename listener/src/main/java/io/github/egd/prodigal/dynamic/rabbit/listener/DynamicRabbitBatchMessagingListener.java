package io.github.egd.prodigal.dynamic.rabbit.listener;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.batch.BatchingStrategy;
import org.springframework.amqp.rabbit.listener.adapter.BatchMessagingMessageListenerAdapter;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

public class DynamicRabbitBatchMessagingListener extends BatchMessagingMessageListenerAdapter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<DynamicRabbitCustomBatchListener> dynamicRabbitCustomBatchListeners;

    public DynamicRabbitBatchMessagingListener(Object bean, Method method, boolean returnExceptions, RabbitListenerErrorHandler errorHandler,
                                               BatchingStrategy batchingStrategy,
                                               List<DynamicRabbitCustomBatchListener> dynamicRabbitCustomBatchListeners) {
        super(bean, method, returnExceptions, errorHandler, batchingStrategy);
        this.dynamicRabbitCustomBatchListeners = dynamicRabbitCustomBatchListeners;
    }

    @Override
    public void onMessageBatch(final List<Message> messages, Channel channel) {
        logger.info("consume message, size: {}", messages.size());
        String consumerQueue = messages.get(0).getMessageProperties().getConsumerQueue();
        this.dynamicRabbitCustomBatchListeners.stream().filter(listener -> listener.canConsumeQueue(consumerQueue)).forEach(listener -> {
            try {
                listener.consume(messages.stream().filter(message -> listener.supportMessageProperties(message.getMessageProperties())).collect(Collectors.toList()), channel);
            } catch (Exception e) {
                logger.error("", e);
            }
        });
//        for (Message message : messages) {
//            this.basicAckQuietly(message.getMessageProperties().getDeliveryTag(), channel);
//        }
    }

    private void basicAckQuietly(long deliveryTag, Channel channel) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            logger.error("", e);
        }
    }

}

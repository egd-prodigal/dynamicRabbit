package io.github.egd.prodigal.dynamic.rabbit.dispatcher;

import com.rabbitmq.client.Channel;
import io.github.egd.prodigal.dynamic.rabbit.listener.DynamicRabbitCustomBatchListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("dynamicRabbitCustomBatchListenerDispatcher")
public class DynamicRabbitCustomBatchListenerDispatcher implements DynamicRabbitCustomBatchListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public boolean canConsumeQueue(String queueName) {
        return true;
    }

    @Override
    public boolean supportMessageProperties(MessageProperties messageProperties) {
        return true;
    }

    @Override
    public void consume(List<Message> messages, Channel channel) {
        logger.info("dispatch messages, size: {}", messages.size());
    }

}

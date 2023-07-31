package io.github.egd.prodigal.dynamic.rabbit.listener;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.List;

public interface DynamicRabbitCustomBatchListener {

    boolean canConsumeQueue(String queueName);

    boolean supportMessageProperties(MessageProperties messageProperties);

    void consume(List<Message> messages, Channel channel);

}

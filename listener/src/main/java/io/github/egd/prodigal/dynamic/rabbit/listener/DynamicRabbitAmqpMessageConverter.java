package io.github.egd.prodigal.dynamic.rabbit.listener;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.SimpleMessageConverter;

public class DynamicRabbitAmqpMessageConverter extends SimpleMessageConverter {

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        return message;
    }
}

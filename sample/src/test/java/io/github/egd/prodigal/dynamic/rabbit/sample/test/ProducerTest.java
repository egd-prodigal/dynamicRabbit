package io.github.egd.prodigal.dynamic.rabbit.sample.test;

import com.rabbitmq.client.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class ProducerTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final RabbitTemplate rabbitTemplate;

    public ProducerTest() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        connectionFactory.setPort(5673);
        connectionFactory.setVirtualHost("/");
        connectionFactory.setUsername("admin");
        connectionFactory.setPassword("password");

        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(connectionFactory);
        cachingConnectionFactory.setPublisherReturns(true);
        cachingConnectionFactory.setChannelCacheSize(10);

        rabbitTemplate = new RabbitTemplate(cachingConnectionFactory);
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setChannelTransacted(true);
        rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {

            private final Logger logger = LoggerFactory.getLogger(getClass());

            @Override
            public void returnedMessage(ReturnedMessage returnedMessage) {
                logger.warn("returned message");
            }

        });
    }

    @Test
    public void send() {
        logger.info("rabbitTemplate: {}", rabbitTemplate);
        Message message = new Message("123".getBytes(), new MessageProperties());
        rabbitTemplate.convertAndSend("demo_exchange", "demo_binding", message);
    }

}

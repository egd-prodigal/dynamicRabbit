package io.github.egd.prodigal.dynamic.rabbit.sample.test;

import com.rabbitmq.client.ConnectionFactory;
import io.github.egd.prodigal.dynamic.rabbit.router.core.RouterConstants;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

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
            public void returnedMessage(@NotNull ReturnedMessage returnedMessage) {
                logger.warn("returned message");
            }

        });
    }

    @Test
    public void send() {
        logger.info("rabbitTemplate: {}", rabbitTemplate);
        for (int i = 0; i < 500; i++) {
            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setHeader(RouterConstants.HEADER_SERVICE_ID, "PROVIDER1");
            messageProperties.setHeader(RouterConstants.HEADER_SERVICE_URL, "/demo/provider");
            Message message = new Message(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8), messageProperties);
            rabbitTemplate.convertAndSend("demo_exchange", "demo_binding", message);
        }
    }

}

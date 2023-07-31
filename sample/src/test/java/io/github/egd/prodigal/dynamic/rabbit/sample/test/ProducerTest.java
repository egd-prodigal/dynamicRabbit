package io.github.egd.prodigal.dynamic.rabbit.sample.test;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
        ExecutorService threadPool = Executors.newFixedThreadPool(16);
        logger.info("rabbitTemplate: {}", rabbitTemplate);
        int count = 100000;
        CountDownLatch countDownLatch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            final String id = (i + 1) + "";
            threadPool.execute(() -> {
                try {
                    Message message = new Message(id.getBytes(), new MessageProperties());
//                    rabbitTemplate.convertAndSend("demo_exchange", "demo_binding", message);
                    rabbitTemplate.execute(channel -> {
                        channel.txSelect();
                        channel.basicPublish("demo_exchange", "demo_binding", new AMQP.BasicProperties(), id.getBytes());
                        channel.txCommit();
                        return null;
                    });
                    TimeUnit.MILLISECONDS.sleep(1);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        threadPool.shutdown();
    }

}

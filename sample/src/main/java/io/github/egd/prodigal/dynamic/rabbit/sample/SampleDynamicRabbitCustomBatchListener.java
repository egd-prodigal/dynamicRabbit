package io.github.egd.prodigal.dynamic.rabbit.sample;

import io.github.egd.prodigal.dynamic.rabbit.listener.DynamicRabbitCustomBatchListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SampleDynamicRabbitCustomBatchListener extends DynamicRabbitCustomBatchListenerAdapter {

    private final Logger logger = LoggerFactory.getLogger(SampleDynamicRabbitCustomBatchListener.class);

    @Override
    protected List<String> supportedQueueNames() {
        List<String> list = new ArrayList<>();
        list.add("demo_queue");
        list.add("demo1");
        list.add("demo2");
        list.add("demo3");
        return list;
    }


    @Override
    public void consume(List<Message> messages) {
        for (Message message : messages) {
            byte[] body = message.getBody();
            String consumerQueue = message.getMessageProperties().getConsumerQueue();
            logger.info("queue: {}, message: {}", consumerQueue, new String(body));
        }
    }

}

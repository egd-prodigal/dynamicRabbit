package io.github.egd.prodigal.dynamic.rabbit.sample;

import io.github.egd.prodigal.dynamic.rabbit.listener.DynamicRabbitCustomBatchListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class SampleDynamicRabbitCustomBatchListener extends DynamicRabbitCustomBatchListenerAdapter {

    private final Logger logger = LoggerFactory.getLogger(SampleDynamicRabbitCustomBatchListener.class);

    @Override
    protected List<String> supportedQueueNames() {
        return Collections.singletonList("demo_queue");
    }


    @Override
    public void consume(List<Message> messages) {
        for (Message message : messages) {
            byte[] body = message.getBody();
            logger.info("message: {}", new String(body));
        }
    }

}

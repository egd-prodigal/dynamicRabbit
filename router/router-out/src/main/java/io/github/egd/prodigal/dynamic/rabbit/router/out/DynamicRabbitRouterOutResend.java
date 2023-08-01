package io.github.egd.prodigal.dynamic.rabbit.router.out;

import io.github.egd.prodigal.dynamic.rabbit.router.core.RouterConstants;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.ByteBuffer;
import java.util.List;

@Component
public class DynamicRabbitRouterOutResend {

    private final RabbitTemplate rabbitTemplate;

    public DynamicRabbitRouterOutResend(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional(rollbackFor = Exception.class)
    public void resend(String exchange, String routingKey, String serviceId, String serviceUrl, List<byte[]> messages) {
        int capacity = 0;
        for (byte[] message : messages) {
            capacity += 4 + message.length;
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(capacity);
        for (byte[] message : messages) {
            byteBuffer.putInt(message.length);
            byteBuffer.put(message);
        }
        byte[] bytes = new byte[capacity];
        byteBuffer.flip();
        byteBuffer.get(bytes);
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader(RouterConstants.HEADER_SERVICE_ID, serviceId);
        messageProperties.setHeader(RouterConstants.HEADER_SERVICE_URL, serviceUrl);
        Message message = new Message(bytes, messageProperties);
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

}

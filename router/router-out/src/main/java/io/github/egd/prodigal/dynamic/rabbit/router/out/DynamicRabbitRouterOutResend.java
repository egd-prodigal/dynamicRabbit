package io.github.egd.prodigal.dynamic.rabbit.router.out;

import io.github.egd.prodigal.dynamic.rabbit.router.core.RouterConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DynamicRabbitRouterOutResend {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final RabbitTemplate rabbitTemplate;

    public DynamicRabbitRouterOutResend(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional(transactionManager = "rabbitTransactionManager", rollbackFor = Exception.class)
    public void resend(String exchange, String routingKey, String serviceId, String serviceUrl, List<byte[]> messages) {
        for (byte[] bytes : messages) {
            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setHeader(RouterConstants.HEADER_SERVICE_ID, serviceId);
            messageProperties.setHeader(RouterConstants.HEADER_SERVICE_URL, serviceUrl);
            Message message = new Message(bytes, messageProperties);
            logger.debug("resend message, exchange: {}, routingKey: {}", exchange, routingKey);
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
        }
    }

}

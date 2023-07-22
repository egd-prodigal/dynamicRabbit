package io.github.egd.prodigal.dynamic.rabbit.listener;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.converter.SimpleMessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DynamicRabbitListenerConfigurer implements RabbitListenerConfigurer {

    private final Logger logger = LoggerFactory.getLogger(DynamicRabbitListenerConfigurer.class);

    private final ApplicationContext applicationContext;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final CachingConnectionFactory dynamicRabbitConsumerCachingConnectionFactory;

    private final Map<String, DynamicRabbitListenerFactoryLoader> dynamicRabbitListenerFactoryLoaderMap;

    private final DefaultMessageHandlerMethodFactory defaultMessageHandlerMethodFactory;

    private final DynamicRabbitAmqpMessageConverter dynamicRabbitAmqpMessageConverter;

    private final Method consumeMethod;


    public DynamicRabbitListenerConfigurer(ApplicationContext applicationContext, BeanFactory beanFactory,
                                           ApplicationEventPublisher applicationEventPublisher,
                                           @Autowired(required = false) Map<String, DynamicRabbitListenerFactoryLoader> dynamicRabbitListenerFactoryLoaderMap,
                                           CachingConnectionFactory dynamicRabbitConsumerCachingConnectionFactory) {
        this.applicationContext = applicationContext;
        this.applicationEventPublisher = applicationEventPublisher;
        this.dynamicRabbitListenerFactoryLoaderMap = dynamicRabbitListenerFactoryLoaderMap;
        this.dynamicRabbitConsumerCachingConnectionFactory = dynamicRabbitConsumerCachingConnectionFactory;
        SimpleMessageConverter simpleMessageConverter = new SimpleMessageConverter();
        this.defaultMessageHandlerMethodFactory = new DefaultMessageHandlerMethodFactory();
        this.defaultMessageHandlerMethodFactory.setMessageConverter(simpleMessageConverter);
        this.defaultMessageHandlerMethodFactory.setBeanFactory(beanFactory);
        List<HandlerMethodArgumentResolver> messageMethodArgumentResolverList = new ArrayList<>();
        messageMethodArgumentResolverList.add(new MessageMethodArgumentResolver());
        this.defaultMessageHandlerMethodFactory.setArgumentResolvers(messageMethodArgumentResolverList);
        this.dynamicRabbitAmqpMessageConverter = new DynamicRabbitAmqpMessageConverter();
        try {
            consumeMethod = getClass().getMethod("consume", org.springframework.messaging.Message.class, Channel.class);
        } catch (NoSuchMethodException e) {
            logger.error("", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar rabbitListenerEndpointRegistrar) {
        if (this.dynamicRabbitListenerFactoryLoaderMap == null || this.dynamicRabbitListenerFactoryLoaderMap.isEmpty()) {
            logger.info("no dynamicRabbitListenerFactoryLoader found");
            return;
        }
        for (Map.Entry<String, DynamicRabbitListenerFactoryLoader> entry : this.dynamicRabbitListenerFactoryLoaderMap.entrySet()) {
            DynamicRabbitListenerFactoryLoader loader = entry.getValue();
            List<DynamicRabbitListenerFactoryProperties> dynamicRabbitListenerFactoryProperties = loader.load();
            for (DynamicRabbitListenerFactoryProperties dynamicRabbitListenerFactoryProperty : dynamicRabbitListenerFactoryProperties) {
                DynamicRabbitMethodRabbitListenerEndpoint rabbitListenerEndpoint = new DynamicRabbitMethodRabbitListenerEndpoint();
                rabbitListenerEndpoint.setId(dynamicRabbitListenerFactoryProperty.getId());
                rabbitListenerEndpoint.setGroup(dynamicRabbitListenerFactoryProperty.getGroup());
                rabbitListenerEndpoint.setAutoStartup(true);
                rabbitListenerEndpoint.setBatchListener(true);
                rabbitListenerEndpoint.setBean(this);
                rabbitListenerEndpoint.setMethod(this.consumeMethod);
                rabbitListenerEndpoint.setMessageHandlerMethodFactory(this.defaultMessageHandlerMethodFactory);
                rabbitListenerEndpoint.setQueueNames(dynamicRabbitListenerFactoryProperty.getQueueNames().toArray(new String[0]));


                SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory = createSimpleMessageListenerContainer(dynamicRabbitListenerFactoryProperty);
                SimpleMessageListenerContainer listenerContainer = simpleRabbitListenerContainerFactory.createListenerContainer(rabbitListenerEndpoint);
                listenerContainer.setListenerId(dynamicRabbitListenerFactoryProperty.getId());
                rabbitListenerEndpoint.setupListenerContainer(listenerContainer);
                rabbitListenerEndpointRegistrar.registerEndpoint(rabbitListenerEndpoint, simpleRabbitListenerContainerFactory);
            }
        }
    }

    private SimpleRabbitListenerContainerFactory createSimpleMessageListenerContainer(DynamicRabbitListenerFactoryProperties dynamicRabbitListenerFactoryProperty) {
        final SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory = new SimpleRabbitListenerContainerFactory();
        simpleRabbitListenerContainerFactory.setConnectionFactory(this.dynamicRabbitConsumerCachingConnectionFactory);
        simpleRabbitListenerContainerFactory.setConcurrentConsumers(dynamicRabbitListenerFactoryProperty.getConcurrentConsumers());
        simpleRabbitListenerContainerFactory.setMaxConcurrentConsumers(dynamicRabbitListenerFactoryProperty.getMaxConcurrentConsumers());
        simpleRabbitListenerContainerFactory.setPrefetchCount(dynamicRabbitListenerFactoryProperty.getPrefetchCount());
        simpleRabbitListenerContainerFactory.setBatchSize(dynamicRabbitListenerFactoryProperty.getBatchSize());
        simpleRabbitListenerContainerFactory.setBatchListener(dynamicRabbitListenerFactoryProperty.isBatchListener());
        simpleRabbitListenerContainerFactory.setConsumerBatchEnabled(dynamicRabbitListenerFactoryProperty.isConsumerBatchEnabled());
        simpleRabbitListenerContainerFactory.setReceiveTimeout(dynamicRabbitListenerFactoryProperty.getReceiveTimeout());
        simpleRabbitListenerContainerFactory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        simpleRabbitListenerContainerFactory.setApplicationContext(this.applicationContext);
        simpleRabbitListenerContainerFactory.setApplicationEventPublisher(this.applicationEventPublisher);
        simpleRabbitListenerContainerFactory.setMessageConverter(this.dynamicRabbitAmqpMessageConverter);
        return simpleRabbitListenerContainerFactory;
    }

    /**
     * 消费消息，是所有消息监听的入口方法
     *
     * @param message 消息，包含一个消息集合
     * @param channel rabbitmq连接channel
     */
    public void consume(org.springframework.messaging.Message<List<Message>> message, Channel channel) {
        List<Message> messages = message.getPayload();
        logger.info("consume message, size: {}", messages.size());
        for (Message amqpMessage : messages) {
            MessageProperties messageProperties = amqpMessage.getMessageProperties();
            long deliveryTag = messageProperties.getDeliveryTag();
            this.basicAckQuietly(deliveryTag, channel);
        }
    }

    private void basicAckQuietly(long deliveryTag, Channel channel) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            logger.error("", e);
        }
    }

}

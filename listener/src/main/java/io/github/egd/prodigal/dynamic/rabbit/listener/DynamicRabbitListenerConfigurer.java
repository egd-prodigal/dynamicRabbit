package io.github.egd.prodigal.dynamic.rabbit.listener;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.messaging.converter.SimpleMessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;

@Component
public class DynamicRabbitListenerConfigurer implements RabbitListenerConfigurer, SchedulingConfigurer {

    private final Logger logger = LoggerFactory.getLogger(DynamicRabbitListenerConfigurer.class);

    private final ApplicationContext applicationContext;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final CachingConnectionFactory dynamicRabbitConsumerCachingConnectionFactory;

    private final Map<String, DynamicRabbitListenerFactoryLoader> dynamicRabbitListenerFactoryLoaderMap;

    private final DefaultMessageHandlerMethodFactory defaultMessageHandlerMethodFactory;

    private final DynamicRabbitAmqpMessageConverter dynamicRabbitAmqpMessageConverter;

    private final Method consumeMethod;

    private RabbitListenerEndpointRegistrar rabbitListenerEndpointRegistrar;

    private RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;

    private final Map<String, DynamicRabbitListenerFactoryProperties> dynamicRabbitListenerFactoryPropertiesMap = new HashMap<>();


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
            consumeMethod = getClass().getMethod("consume");
        } catch (NoSuchMethodException e) {
            logger.error("", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar rabbitListenerEndpointRegistrar) {
        this.rabbitListenerEndpointRegistrar = rabbitListenerEndpointRegistrar;
        if (this.dynamicRabbitListenerFactoryLoaderMap == null || this.dynamicRabbitListenerFactoryLoaderMap.isEmpty()) {
            logger.info("no dynamicRabbitListenerFactoryLoader found");
            return;
        }

        registerEndpoints();
    }

    public void registerEndpoints() {
        logger.info("register endpoints");
        final Set<String> registeredEndpointIdSet = this.dynamicRabbitListenerFactoryPropertiesMap.keySet();
        final Set<String> newEndpointIdSet = new HashSet<>();
        for (Map.Entry<String, DynamicRabbitListenerFactoryLoader> entry : this.dynamicRabbitListenerFactoryLoaderMap.entrySet()) {
            DynamicRabbitListenerFactoryLoader loader = entry.getValue();
            List<DynamicRabbitListenerFactoryProperties> dynamicRabbitListenerFactoryProperties = loader.load();
            for (DynamicRabbitListenerFactoryProperties dynamicRabbitListenerFactoryProperty : dynamicRabbitListenerFactoryProperties) {
                String id = dynamicRabbitListenerFactoryProperty.getId();
                if (!registeredEndpointIdSet.contains(id)) {
                    registerEndpoint(dynamicRabbitListenerFactoryProperty);
                } else {
                    DynamicRabbitListenerFactoryProperties registeredListenerFactoryProperties = this.dynamicRabbitListenerFactoryPropertiesMap.get(id);
                    if (!registeredListenerFactoryProperties.equals(dynamicRabbitListenerFactoryProperty)) {
                        stopMessageListener(id);
                        registerEndpoint(dynamicRabbitListenerFactoryProperty);
                    }
                }
                newEndpointIdSet.add(dynamicRabbitListenerFactoryProperty.getId());
            }
        }
        registeredEndpointIdSet.stream().filter(id -> !newEndpointIdSet.contains(id))
                .forEach(this::stopMessageListener);
    }

    private void registerEndpoint(DynamicRabbitListenerFactoryProperties dynamicRabbitListenerFactoryProperty) {
        DynamicRabbitMethodRabbitListenerEndpoint rabbitListenerEndpoint = new DynamicRabbitMethodRabbitListenerEndpoint(applicationContext);
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
        this.rabbitListenerEndpointRegistrar.registerEndpoint(rabbitListenerEndpoint, simpleRabbitListenerContainerFactory);

        dynamicRabbitListenerFactoryPropertiesMap.put(dynamicRabbitListenerFactoryProperty.getId(), dynamicRabbitListenerFactoryProperty);
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
     * 消费消息，空方法，仅用于注册endpoint，实际不调用本方法消费
     *
     * @see DynamicRabbitBatchMessagingListener#onMessageBatch(List, Channel)
     */
    public void consume() {
    }

    private void stopMessageListener(String id) {
        if (this.rabbitListenerEndpointRegistry == null) {
            this.rabbitListenerEndpointRegistry = this.rabbitListenerEndpointRegistrar.getEndpointRegistry();
        }
        Optional.ofNullable(this.rabbitListenerEndpointRegistry)
                .flatMap(registry -> Optional.ofNullable(registry.unregisterListenerContainer(id))
                        .filter(e -> e instanceof SimpleMessageListenerContainer)
                        .map(SimpleMessageListenerContainer.class::cast))
                .ifPresent(SimpleMessageListenerContainer::destroy);

        this.dynamicRabbitListenerFactoryPropertiesMap.remove(id);
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        Environment environment = this.applicationContext.getEnvironment();
        String cron = environment.getProperty("dynamic.rabbit.refresh.cron", "0 0/5 * * * ?");
        taskRegistrar.addCronTask(this::registerEndpoints, cron);
    }

}

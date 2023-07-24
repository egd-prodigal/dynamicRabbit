package io.github.egd.prodigal.dynamic.rabbit.listener;

import com.rabbitmq.client.ConnectionFactory;
import io.github.egd.prodigal.dynamic.rabbit.core.DynamicRabbitCoreConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.batch.BatchingStrategy;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@AutoConfiguration(after = DynamicRabbitCoreConfiguration.class)
@Configuration
@EnableScheduling
public class DynamicRabbitListenerConfiguration {

    private final Logger logger = LoggerFactory.getLogger(DynamicRabbitListenerConfiguration.class);

    /**
     * 消费者连接工厂
     *
     * @param connectionFactory spring rabbit
     */
    @Bean("dynamicRabbitConsumerCachingConnectionFactory")
    @Primary
    public CachingConnectionFactory dynamicRabbitConsumerCachingConnectionFactory(
            @Autowired(required = false) @Qualifier("dynamicRabbitConnectionFactory") ConnectionFactory connectionFactory) {
        final CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(connectionFactory);
        cachingConnectionFactory.setConnectionThreadFactory(new ThreadFactory() {
            private final AtomicInteger i = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "dr-ccf-" + i.incrementAndGet());
            }
        });
        cachingConnectionFactory.setCacheMode(CachingConnectionFactory.CacheMode.CHANNEL);
        cachingConnectionFactory.setChannelCheckoutTimeout(60000L);
        cachingConnectionFactory.setChannelCacheSize(512);
        cachingConnectionFactory.addChannelListener((channel, transactional) -> logger.debug("create channel: {}, transactional: {}", channel, transactional));
        return cachingConnectionFactory;
    }

    /**
     * 监听配置器
     *
     * @param applicationEventPublisher             时间发布器
     * @param applicationContext                    应用上下文
     * @param dynamicRabbitListenerFactoryLoaderMap 监听配置加载器
     * @return io.github.egd.prodigal.dynamic.rabbit.listener.DynamicRabbitListenerConfigurer
     */
    @Bean("dynamicRabbitListenerConfigurer")
    public DynamicRabbitListenerConfigurer dynamicRabbitListenerConfigurer(@Autowired ApplicationEventPublisher applicationEventPublisher,
                                                                           @Autowired ApplicationContext applicationContext,
                                                                           @Autowired BeanFactory beanFactory,
                                                                           @Autowired(required = false) Map<String, DynamicRabbitListenerFactoryLoader> dynamicRabbitListenerFactoryLoaderMap,
                                                                           @Autowired @Qualifier("dynamicRabbitConsumerCachingConnectionFactory") CachingConnectionFactory dynamicRabbitConsumerCachingConnectionFactory) {
        return new DynamicRabbitListenerConfigurer(applicationContext, beanFactory, applicationEventPublisher,
                dynamicRabbitListenerFactoryLoaderMap, dynamicRabbitConsumerCachingConnectionFactory);
    }

    @Bean
    @Scope("prototype")
    public DynamicRabbitBatchMessagingListener dynamicRabbitBatchMessagingListener(@Autowired(required = false) String id,
                                                                                   @Autowired(required = false) String group,
                                                                                   @Autowired(required = false) Collection<String> queueNames,
                                                                                   @Autowired(required = false) DynamicRabbitListenerConfigurer bean,
                                                                                   @Autowired(required = false) Method method,
                                                                                   @Autowired(required = false) BatchingStrategy batchingStrategy,
                                                                                   @Autowired(required = false) List<DynamicRabbitCustomBatchListener> dynamicRabbitCustomBatchListeners) {
        logger.info("create dynamicRabbitBatchMessagingListener, id: {}, group: {}, queueNames: {}", id, group, queueNames);
        return new DynamicRabbitBatchMessagingListener(bean, method, true,
                dynamicLoggingRabbitListenerErrorHandler(), batchingStrategy, dynamicRabbitCustomBatchListeners);
    }

    @Bean
    public RabbitListenerErrorHandler dynamicLoggingRabbitListenerErrorHandler() {
        return new RabbitListenerErrorHandler() {

            private final Logger logger = LoggerFactory.getLogger("logging.rabbit.listener.error.handler");

            @Override
            public Object handleError(Message message, org.springframework.messaging.Message<?> message1, ListenerExecutionFailedException e) {
                logger.error("", e);
                return null;
            }
        };
    }

}

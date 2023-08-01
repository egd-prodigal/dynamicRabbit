package io.github.egd.prodigal.dynamic.rabbit.router.out;

import com.rabbitmq.client.ConnectionFactory;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.transaction.RabbitTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.ClientHttpRequestFactorySupplier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.time.Duration;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class DynamicRabbitRouterOutConfig {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofMinutes(5))
                .setReadTimeout(Duration.ofMinutes(5))
                .defaultHeader("content-type", "application/octet-stream")
                .requestFactory(() -> new OkHttp3ClientHttpRequestFactory(okHttpClient()))
                .build();
        restTemplate.getMessageConverters().stream().filter(e -> e instanceof StringHttpMessageConverter)
                .forEach(e -> ((StringHttpMessageConverter) e).setDefaultCharset(Charset.defaultCharset()));
        return restTemplate;
    }

    @Bean
    public OkHttpClient okHttpClient() {
        ConnectionPool connectionPool = new ConnectionPool(200, 5, TimeUnit.MINUTES);
        return new OkHttpClient.Builder()
                .connectionPool(connectionPool)
                .callTimeout(Duration.ofMinutes(5))
                .connectTimeout(Duration.ofMinutes(5))
                .writeTimeout(Duration.ofMinutes(5))
                .readTimeout(Duration.ofMinutes(5))
                .followRedirects(true)
                .build();
    }

    @Bean("producerCachingConnectionFactory")
    public CachingConnectionFactory producerCachingConnectionFactory(@Autowired(required = false)
                                                                     @Qualifier("dynamicRabbitConnectionFactory") ConnectionFactory connectionFactory) {
        final CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(connectionFactory);
        cachingConnectionFactory.setCacheMode(CachingConnectionFactory.CacheMode.CHANNEL);
        cachingConnectionFactory.setChannelCheckoutTimeout(60000L);
        cachingConnectionFactory.setChannelCacheSize(200);
        cachingConnectionFactory.setPublisherReturns(true);
        return cachingConnectionFactory;
    }

    @Bean("rabbitTransactionManager")
    public RabbitTransactionManager rabbitTransactionManager(@Autowired @Qualifier("producerCachingConnectionFactory") CachingConnectionFactory producerCachingConnectionFactory) {
        return new RabbitTransactionManager(producerCachingConnectionFactory);
    }

    @Bean("transactionRabbitTemplate")
    public RabbitTemplate transactionRabbitTemplate(@Autowired @Qualifier("producerCachingConnectionFactory") CachingConnectionFactory producerCachingConnectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(producerCachingConnectionFactory);
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setChannelTransacted(true);
        return rabbitTemplate;
    }

    @Bean("retryRabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory retryRabbitListenerContainerFactory(@Autowired @Qualifier("dynamicRabbitConsumerCachingConnectionFactory") CachingConnectionFactory consumerCachingConnectionFactory) {
        final SimpleRabbitListenerContainerFactory containerFactory = new SimpleRabbitListenerContainerFactory();
        containerFactory.setConnectionFactory(consumerCachingConnectionFactory);
        containerFactory.setBatchSize(10);
        containerFactory.setBatchListener(true);
        containerFactory.setMaxConcurrentConsumers(4);
        containerFactory.setConsumerBatchEnabled(true);
        containerFactory.setPrefetchCount(40);
        containerFactory.setReceiveTimeout(30000L);
        return containerFactory;
    }

    @Bean("router-out-retry-queue")
    public Queue retryQueue() {
        Queue queue = new Queue("router-out-retry");
        queue.getArguments().put("x-queue-mode", "lazy");
        return queue;
    }

    @Bean("router-out-exchange")
    public DirectExchange routerOutExchange() {
        return new DirectExchange("router-out");
    }

    @Bean("router-out-retry-binding")
    public Binding routerRetryBinding(@Autowired Queue retryQueue, @Autowired DirectExchange routerOutExchange) {
        return BindingBuilder.bind(retryQueue).to(routerOutExchange).with("retry-routing");
    }


}

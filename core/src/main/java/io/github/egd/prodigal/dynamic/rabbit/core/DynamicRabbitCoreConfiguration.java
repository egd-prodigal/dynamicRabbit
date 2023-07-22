package io.github.egd.prodigal.dynamic.rabbit.core;

import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class DynamicRabbitCoreConfiguration {

    /**
     * rabbit连接工厂，最底层的工具
     *
     * @param rabbitProperties spring rabbit 相关配置
     */
    @Bean("dynamicRabbitConnectionFactory")
    public ConnectionFactory dynamicRabbitConnectionFactory(@Autowired(required = false) RabbitProperties rabbitProperties) throws IllegalAccessException {
        if (rabbitProperties == null) {
            throw new IllegalAccessException("no spring rabbit properties found");
        }
        final ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(rabbitProperties.getHost());
        connectionFactory.setPort(rabbitProperties.getPort());
        connectionFactory.setUsername(rabbitProperties.getUsername());
        connectionFactory.setPassword(rabbitProperties.getPassword());
        connectionFactory.setVirtualHost(rabbitProperties.getVirtualHost());
        connectionFactory.setAutomaticRecoveryEnabled(true);
        connectionFactory.setConnectionTimeout((int) (rabbitProperties.getConnectionTimeout().getSeconds() * 1000));
        connectionFactory.setRequestedChannelMax(rabbitProperties.getRequestedChannelMax());
        connectionFactory.setChannelRpcTimeout((int) (rabbitProperties.getChannelRpcTimeout().getSeconds() * 1000));
        return connectionFactory;
    }



}

package io.github.egd.prodigal.sample.provider1;

import io.github.egd.prodigal.dynamic.rabbit.router.core.ByteArrayDeserializer;
import io.github.egd.prodigal.dynamic.rabbit.router.core.MessageParamArgumentResolver;
import io.github.egd.prodigal.dynamic.rabbit.router.core.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.serializer.DefaultDeserializer;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Configuration
public class Provider1WebConfig extends WebMvcConfigurationSupport {

    @Bean
    public MessageParamArgumentResolver messageParamArgumentResolver() {
        return new MessageParamArgumentResolver();
    }

    @Bean
    public DefaultDeserializer defaultDeserializer() {
        return new DefaultDeserializer();
    }

    @Bean
    public StringDeserializer stringDeserializer() {
        return new StringDeserializer();
    }

    @Bean
    public ByteArrayDeserializer byteArrayDeserializer() {
        return new ByteArrayDeserializer();
    }

    @Override
    protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(messageParamArgumentResolver());
    }

}

package io.github.egd.prodigal.dynamic.rabbit.sample;

import io.github.egd.prodigal.dynamic.rabbit.router.core.MessageParamArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.serializer.DefaultDeserializer;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Configuration
public class SampleConfig extends WebMvcConfigurationSupport {

    @Bean
    public MessageParamArgumentResolver messageParamArgumentResolver() {
        return new MessageParamArgumentResolver();
    }

    @Bean
    public DefaultDeserializer defaultDeserializer() {
        return new DefaultDeserializer();
    }

    @Override
    protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(messageParamArgumentResolver());
    }

}

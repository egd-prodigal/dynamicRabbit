package io.github.egd.prodigal.dynamic.rabbit.sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.serializer.DefaultDeserializer;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Configuration
public class SampleConfig extends WebMvcConfigurationSupport {

    private final MessageParamArgumentResolver messageParamArgumentResolver;

    public SampleConfig(MessageParamArgumentResolver messageParamArgumentResolver) {
        this.messageParamArgumentResolver = messageParamArgumentResolver;
    }

    @Bean
    public DefaultDeserializer defaultDeserializer() {
        return new DefaultDeserializer();
    }

    @Override
    protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(messageParamArgumentResolver);
    }


}

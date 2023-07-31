package io.github.egd.prodigal.dynamic.rabbit.router.out;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.boot.web.client.ClientHttpRequestFactorySupplier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

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

}

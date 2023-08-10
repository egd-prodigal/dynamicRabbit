package io.github.egd.prodigal.dynamic.rabbit.router.in;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@EnableAsync
public class DynamicRabbitRouterInConfig extends WebMvcConfigurationSupport {

    @Override
    protected void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(getTaskExecutor());
    }

    @Bean("holderTaskExecutor")
    protected ConcurrentTaskExecutor getTaskExecutor() {
        return new ConcurrentTaskExecutor(holderExecutor());
    }

    @Bean("holderExecutor")
    public ThreadPoolExecutor holderExecutor() {
        return new ThreadPoolExecutor(64, 2048,
                300, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadFactory() {
                    private final AtomicInteger index = new AtomicInteger();

                    @Override
                    public Thread newThread(@NonNull Runnable r) {
                        return new Thread(r, "holder-" + index.incrementAndGet());
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean("handlerExecutor")
    public ThreadPoolExecutor handlerExecutor() {
        return new ThreadPoolExecutor(16, 256,
                600, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadFactory() {
                    private final AtomicInteger index = new AtomicInteger();

                    @Override
                    public Thread newThread(@NonNull Runnable r) {
                        return new Thread(r, "handler-" + index.incrementAndGet());
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy());
    }


}

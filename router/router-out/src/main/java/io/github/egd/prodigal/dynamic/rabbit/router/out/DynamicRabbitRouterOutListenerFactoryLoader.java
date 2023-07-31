package io.github.egd.prodigal.dynamic.rabbit.router.out;

import io.github.egd.prodigal.dynamic.rabbit.listener.DynamicRabbitListenerFactoryLoader;
import io.github.egd.prodigal.dynamic.rabbit.listener.DynamicRabbitListenerFactoryProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class DynamicRabbitRouterOutListenerFactoryLoader implements DynamicRabbitListenerFactoryLoader {

    @Override
    public List<DynamicRabbitListenerFactoryProperties> load() {
        final DynamicRabbitListenerFactoryProperties properties = new DynamicRabbitListenerFactoryProperties();
        properties.setGroup("default");
        properties.setId("demo-endpoint");
        properties.setQueueNames(Collections.singletonList("demo_queue"));
        properties.setBatchSize(100);
        properties.setPrefetchCount(500);
        properties.setBatchListener(true);
        properties.setMaxConcurrentConsumers(8);
        properties.setReceiveTimeout(1000L);
        return Collections.singletonList(properties);
    }

}

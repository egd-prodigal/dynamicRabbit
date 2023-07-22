package io.github.egd.prodigal.dynamic.rabbit.sample;

import io.github.egd.prodigal.dynamic.rabbit.listener.DynamicRabbitListenerFactoryLoader;
import io.github.egd.prodigal.dynamic.rabbit.listener.DynamicRabbitListenerFactoryProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class SampleRabbitListenerFactoryLoader implements DynamicRabbitListenerFactoryLoader {
    @Override
    public List<DynamicRabbitListenerFactoryProperties> load() {
        DynamicRabbitListenerFactoryProperties properties = new DynamicRabbitListenerFactoryProperties();
        properties.setGroup("default");
        properties.setId("demo-endpoint");
        properties.setQueueNames(Collections.singletonList("demo_queue"));
        return Collections.singletonList(properties);
    }

}

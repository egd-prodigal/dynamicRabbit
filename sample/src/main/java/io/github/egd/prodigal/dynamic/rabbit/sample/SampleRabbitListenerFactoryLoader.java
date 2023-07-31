package io.github.egd.prodigal.dynamic.rabbit.sample;

import io.github.egd.prodigal.dynamic.rabbit.listener.DynamicRabbitListenerFactoryLoader;
import io.github.egd.prodigal.dynamic.rabbit.listener.DynamicRabbitListenerFactoryProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Component
public class SampleRabbitListenerFactoryLoader implements DynamicRabbitListenerFactoryLoader {

//    private final SampleDynamicRabbitDatabasePropertiesMapper mapper;
//
//    public SampleRabbitListenerFactoryLoader(SampleDynamicRabbitDatabasePropertiesMapper mapper) {
//        this.mapper = mapper;
//    }

    @Override
    public List<DynamicRabbitListenerFactoryProperties> load() {
        DynamicRabbitListenerFactoryProperties properties = new DynamicRabbitListenerFactoryProperties();
        properties.setGroup("default");
        properties.setId("demo-endpoint");
        properties.setQueueNames(Collections.singletonList("demo_queue"));
        properties.setBatchSize(50);
        properties.setBatchListener(true);
        properties.setPrefetchCount(200);
        properties.setMaxConcurrentConsumers(4);
        properties.setConsumerBatchEnabled(true);
        properties.setReceiveTimeout(10000L);
        return Collections.singletonList(properties);

//        List<DynamicRabbitListenerFactoryProperties> dynamicRabbitListenerFactoryProperties = mapper.queryAll();
//        Iterator<DynamicRabbitListenerFactoryProperties> it = dynamicRabbitListenerFactoryProperties.iterator();
//        while (it.hasNext()) {
//            DynamicRabbitListenerFactoryProperties listenerFactoryProperties = it.next();
//            List<String> queueNames = mapper.queryQueueNamesById(listenerFactoryProperties.getId());
//            if (queueNames == null || queueNames.isEmpty()) {
//                it.remove();
//                continue;
//            }
//            listenerFactoryProperties.setQueueNames(queueNames);
//        }
//        return dynamicRabbitListenerFactoryProperties;
    }

}

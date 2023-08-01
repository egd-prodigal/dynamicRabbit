package io.github.egd.prodigal.dynamic.rabbit.router.out;

import io.github.egd.prodigal.dynamic.rabbit.listener.DynamicRabbitListenerFactoryLoader;
import io.github.egd.prodigal.dynamic.rabbit.listener.DynamicRabbitListenerFactoryProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class DynamicRabbitRouterOutListenerFactoryLoader implements DynamicRabbitListenerFactoryLoader {

    private final DynamicRabbitRouterOutListenerMapper dynamicRabbitRouterOutListenerMapper;

    public DynamicRabbitRouterOutListenerFactoryLoader(DynamicRabbitRouterOutListenerMapper dynamicRabbitRouterOutListenerMapper) {
        this.dynamicRabbitRouterOutListenerMapper = dynamicRabbitRouterOutListenerMapper;
    }

    @Override
    public List<DynamicRabbitListenerFactoryProperties> load() {
        List<DynamicRabbitListenerFactoryProperties> dynamicRabbitListenerFactoryProperties = dynamicRabbitRouterOutListenerMapper.selectAll();
        if (!CollectionUtils.isEmpty(dynamicRabbitListenerFactoryProperties)) {
            for (DynamicRabbitListenerFactoryProperties dynamicRabbitListenerFactoryProperty : dynamicRabbitListenerFactoryProperties) {
                String queueNamesString = dynamicRabbitRouterOutListenerMapper.selectQueueNames(dynamicRabbitListenerFactoryProperty.getId());
                List<String> queueNames = new ArrayList<>(Arrays.asList(queueNamesString.split(",")));
                dynamicRabbitListenerFactoryProperty.setQueueNames(queueNames);
            }
        }
        return dynamicRabbitListenerFactoryProperties;
    }

}

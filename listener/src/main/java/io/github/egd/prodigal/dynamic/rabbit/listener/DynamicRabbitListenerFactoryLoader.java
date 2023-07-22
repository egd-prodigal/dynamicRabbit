package io.github.egd.prodigal.dynamic.rabbit.listener;

import java.util.List;

public interface DynamicRabbitListenerFactoryLoader {

    List<DynamicRabbitListenerFactoryProperties> load();

}

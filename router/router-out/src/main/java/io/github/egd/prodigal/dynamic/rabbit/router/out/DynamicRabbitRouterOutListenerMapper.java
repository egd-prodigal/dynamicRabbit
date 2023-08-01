package io.github.egd.prodigal.dynamic.rabbit.router.out;

import io.github.egd.prodigal.dynamic.rabbit.listener.DynamicRabbitListenerFactoryProperties;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DynamicRabbitRouterOutListenerMapper {

    @Select({"select endpoint_id as id, 'router-out' as `group`, max_concurrent_consumers as maxConcurrentConsumers,",
            "prefetch_count as prefetchCount, batch_size as batchSize, receive_timeout as receiveTimeout",
            "from router_endpoint_config where listen = 1 and queue_names is not null"})
    List<DynamicRabbitListenerFactoryProperties> selectAll();

    @Select("select queue_names from router_endpoint_config where endpoint_id = #{endpoint_id} and listen = 1")
    String selectQueueNames(@Param("endpoint_id") String endpointId);

}

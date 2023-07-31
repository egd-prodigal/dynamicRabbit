package io.github.egd.prodigal.dynamic.rabbit.sample;

//import io.github.egd.prodigal.dynamic.rabbit.listener.DynamicRabbitListenerFactoryProperties;
//import org.apache.ibatis.annotations.Mapper;
//import org.apache.ibatis.annotations.Param;
//import org.apache.ibatis.annotations.Select;
//
//import java.util.List;

//@Mapper
//public interface SampleDynamicRabbitDatabasePropertiesMapper {
//
//    @Select({"select id as id ,`group` as `group`, concurrent_consumers as concurrentConsumers,",
//            "max_concurrent_consumers as maxConcurrentConsumers, prefetch_count as prefetchCount,",
//            "batch_listener as batchListener, batch_size as batchSize, consumer_batch_enabled as consumerBatchEnabled,",
//            "receive_timeout as receiveTimeout from sample_drq_listener"})
//    List<DynamicRabbitListenerFactoryProperties> queryAll();
//
//    @Select("select queue_name from sample_drq_listener_queue_names where listener_id = #{listenerId, jdbcType=VARCHAR}")
//    List<String> queryQueueNamesById(@Param("listenerId") String listenerId);
//
//}

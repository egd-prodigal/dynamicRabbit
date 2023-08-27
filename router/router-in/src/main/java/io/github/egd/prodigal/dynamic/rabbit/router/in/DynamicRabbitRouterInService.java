package io.github.egd.prodigal.dynamic.rabbit.router.in;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class DynamicRabbitRouterInService {

    private final Logger logger = LoggerFactory.getLogger(DynamicRabbitRouterInService.class);

    private final RingBuffer<DynamicRabbitRouterInAsyncHolder> ringBuffer;

    public DynamicRabbitRouterInService(DynamicRabbitRouterInEventHandler eventHandler) {
        final AtomicInteger index = new AtomicInteger();
        final Disruptor<DynamicRabbitRouterInAsyncHolder> disruptor = new Disruptor<>(DynamicRabbitRouterInAsyncHolder::new, 16384, r -> {
            return new Thread(r, "event-" + index.incrementAndGet());
        }, ProducerType.MULTI, new YieldingWaitStrategy());
        disruptor.handleEventsWith(eventHandler);
        this.ringBuffer = disruptor.start();
    }

    public Object send(String exchange, String routingKey, byte[] bytes) throws ExecutionException, InterruptedException {
//        logger.info("exchange: {}, routingKey: {}, data size: {}", exchange, routingKey, bytes.length);
        AtomicReference<DynamicRabbitRouterInAsyncHolder> ref = new AtomicReference<>();
        ringBuffer.publishEvent((dynamicRabbitRouterInAsyncHolder, l) -> {
            dynamicRabbitRouterInAsyncHolder.setExchange(exchange);
            dynamicRabbitRouterInAsyncHolder.setRoutingKey(routingKey);
            dynamicRabbitRouterInAsyncHolder.setBytes(bytes);
            ref.set(dynamicRabbitRouterInAsyncHolder);
        });
        return ref.get().get();
    }

}

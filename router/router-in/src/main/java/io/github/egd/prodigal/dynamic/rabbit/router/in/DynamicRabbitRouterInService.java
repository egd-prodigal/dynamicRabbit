package io.github.egd.prodigal.dynamic.rabbit.router.in;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.TimeoutBlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class DynamicRabbitRouterInService {

    private final Logger logger = LoggerFactory.getLogger(DynamicRabbitRouterInService.class);


    private final RingBuffer<DynamicRabbitRouterInAsyncHolder> ringBuffer;

    public DynamicRabbitRouterInService(@Qualifier("handlerExecutor") ThreadPoolExecutor handlerExecutor, DynamicRabbitRouterInSender routerInSender) {
        final Disruptor<DynamicRabbitRouterInAsyncHolder> disruptor = new Disruptor<>(DynamicRabbitRouterInAsyncHolder::new, 1024, r -> {
            return new Thread(r);
        }, ProducerType.MULTI, new TimeoutBlockingWaitStrategy(10, TimeUnit.MILLISECONDS));
        disruptor.handleEventsWith(new EventHandler<DynamicRabbitRouterInAsyncHolder>() {

            private List<DynamicRabbitRouterInAsyncHolder> buffer = new ArrayList<>();

            private int byteLength = 0;

            private long timeout = System.currentTimeMillis() + 50;

            {
                Executors.newSingleThreadScheduledExecutor()
                        .scheduleWithFixedDelay(() -> onEvent(null, -1, false)
                                , 100, 100, TimeUnit.MILLISECONDS);
            }

            @Override
            public void onEvent(DynamicRabbitRouterInAsyncHolder dynamicRabbitRouterInAsyncHolder, long l, boolean b) {
                if (l != -1) {
                    byteLength += dynamicRabbitRouterInAsyncHolder.byteLength();
                    buffer.add(dynamicRabbitRouterInAsyncHolder);
                }
                if (buffer.size() >= 50 || byteLength >= 20 * 1024 * 1024 || timeout < System.currentTimeMillis()) {
                    if (!buffer.isEmpty()) {
                        final List<DynamicRabbitRouterInAsyncHolder> list = buffer;
                        handlerExecutor.execute(() -> routerInSender.send(list));
                        buffer = new ArrayList<>();
                        byteLength = 0;
                        timeout = System.currentTimeMillis() + 50;
                    }
                }
            }
        });
        this.ringBuffer = disruptor.start();
    }

    public Object send(String exchange, String routingKey, byte[] bytes) throws ExecutionException, InterruptedException {
        logger.info("exchange: {}, routingKey: {}, data size: {}", exchange, routingKey, bytes.length);
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

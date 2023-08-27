package io.github.egd.prodigal.dynamic.rabbit.router.in;

import com.lmax.disruptor.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Component
public class DynamicRabbitRouterInEventHandler implements EventHandler<DynamicRabbitRouterInAsyncHolder> {

    private final Logger logger = LoggerFactory.getLogger(DynamicRabbitRouterInEventHandler.class);

    private List<DynamicRabbitRouterInAsyncHolder> buffer = new ArrayList<>();

    private int byteLength = 0;

    private long timeout = System.currentTimeMillis() + 300;

    private final DynamicRabbitRouterInSender routerInSender;

    private final ThreadPoolExecutor handlerExecutor;

    public DynamicRabbitRouterInEventHandler(@Qualifier("handlerExecutor") ThreadPoolExecutor handlerExecutor, DynamicRabbitRouterInSender routerInSender) {
        this.routerInSender = routerInSender;
        this.handlerExecutor = handlerExecutor;
    }

    @Override
    public void onEvent(DynamicRabbitRouterInAsyncHolder dynamicRabbitRouterInAsyncHolder, long sequence, boolean endOfBatch) {
        if (sequence != -1 && dynamicRabbitRouterInAsyncHolder != null) {
//            logger.info("onEvent, sequence: {}, endOfBatch: {}", sequence, endOfBatch);
            byteLength += dynamicRabbitRouterInAsyncHolder.byteLength();
            buffer.add(dynamicRabbitRouterInAsyncHolder);
        }
        if (buffer.size() >= 100 || byteLength >= 20 * 1024 * 1024 || timeout < System.currentTimeMillis()) {
            if (!buffer.isEmpty()) {
                final List<DynamicRabbitRouterInAsyncHolder> list = buffer;
                handlerExecutor.execute(() -> routerInSender.send(list));
                buffer = new ArrayList<>();
                byteLength = 0;
                timeout = System.currentTimeMillis() + 300;
            }
        }
    }

    @Scheduled(cron = "0/1 * * * * ?")
    public void heartBeat() {
        onEvent(null, -1, false);
    }

}

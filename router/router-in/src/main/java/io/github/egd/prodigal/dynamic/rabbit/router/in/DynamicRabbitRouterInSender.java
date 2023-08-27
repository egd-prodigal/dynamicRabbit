package io.github.egd.prodigal.dynamic.rabbit.router.in;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DynamicRabbitRouterInSender {

    private final Logger logger = LoggerFactory.getLogger(DynamicRabbitRouterInSender.class);


    public void send(List<DynamicRabbitRouterInAsyncHolder> list) {
        logger.info("send, size: {}", list.size());
        try {
            TimeUnit.MILLISECONDS.sleep(500L);
        } catch (InterruptedException e) {
        }
        for (DynamicRabbitRouterInAsyncHolder asyncHolder : list) {
//            logger.info("send data, exchange: {}, routingKey: {}, data size: {}", asyncHolder.getExchange(), asyncHolder.getRoutingKey(), asyncHolder.getBytes().length);
            asyncHolder.complete(DynamicRabbitRouterInResultEnum.SUCCESS.getCode());
        }
    }

}

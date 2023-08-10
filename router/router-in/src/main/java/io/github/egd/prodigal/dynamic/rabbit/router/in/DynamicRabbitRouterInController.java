package io.github.egd.prodigal.dynamic.rabbit.router.in;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.WebAsyncTask;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.Callable;

@RestController
public class DynamicRabbitRouterInController {

    private final Logger logger = LoggerFactory.getLogger(DynamicRabbitRouterInController.class);

    private final DynamicRabbitRouterInService service;

    public DynamicRabbitRouterInController(DynamicRabbitRouterInService service) {
        this.service = service;
    }

    @RequestMapping(value = "/{exchange}/{routingKey}", method = {RequestMethod.PUT, RequestMethod.POST})
    public WebAsyncTask<Object> send(@PathVariable String exchange, @PathVariable String routingKey,
                         HttpServletRequest request) throws IOException {
        byte[] bytes = StreamUtils.copyToByteArray(request.getInputStream());
        Callable<Object> callable = () -> service.send(exchange, routingKey, bytes);
        WebAsyncTask<Object> webAsyncTask = new WebAsyncTask<>(300000L, "holderTaskExecutor", callable);
        webAsyncTask.onCompletion(() -> {});
        webAsyncTask.onTimeout(DynamicRabbitRouterInResultEnum.TIMEOUT::getCode);
        webAsyncTask.onError(DynamicRabbitRouterInResultEnum.FAIL::getCode);
        return webAsyncTask;
    }

}

package io.github.egd.prodigal.dynamic.rabbit.router.out;

import io.github.egd.prodigal.dynamic.rabbit.listener.DynamicRabbitCustomBatchListener;
import io.github.egd.prodigal.dynamic.rabbit.router.core.RouterConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DynamicRabbitRouterOutBatchListener implements DynamicRabbitCustomBatchListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final RestTemplate restTemplate;

    private final DynamicRabbitRouterOutResend dynamicRabbitRouterOutResend;

    public DynamicRabbitRouterOutBatchListener(RestTemplate restTemplate, DynamicRabbitRouterOutResend dynamicRabbitRouterOutResend) {
        this.restTemplate = restTemplate;
        this.dynamicRabbitRouterOutResend = dynamicRabbitRouterOutResend;
    }

    @Override
    public boolean canConsumeQueue(String queueName) {
        return true;
    }

    @Override
    public boolean supportMessageProperties(MessageProperties messageProperties) {
        String serviceId = messageProperties.getHeader(RouterConstants.HEADER_SERVICE_ID);
        String serviceUrl = messageProperties.getHeader(RouterConstants.HEADER_SERVICE_URL);
        return serviceId != null && !"".equals(serviceId) && serviceUrl != null && !"".equals(serviceUrl);
    }

    @Override
    @RabbitListener(queues = "router-out-retry", group = "router-out", containerFactory = "retryRabbitListenerContainerFactory")
    public void consume(List<Message> messages) {
        Map<String, Map<String, List<byte[]>>> map = new HashMap<>();
        for (Message message : messages) {
            MessageProperties messageProperties = message.getMessageProperties();
            String serviceId = messageProperties.getHeader(RouterConstants.HEADER_SERVICE_ID);
            String serviceUrl = messageProperties.getHeader(RouterConstants.HEADER_SERVICE_URL);
            if (serviceId == null || "".equals(serviceId) || serviceUrl == null || "".equals(serviceUrl)) {
                continue;
            }
            if (!serviceUrl.startsWith("/")) {
                serviceUrl = "/" + serviceUrl;
            }
            map.putIfAbsent(serviceId, new HashMap<>());
            Map<String, List<byte[]>> serviceIdMap = map.get(serviceId);
            serviceIdMap.putIfAbsent(serviceUrl, new ArrayList<>());
            serviceIdMap.get(serviceUrl).add(message.getBody());
        }
        map.forEach((serviceId, m) ->
                m.forEach((serviceUrl, bytesList) -> {
                    try {
                        logger.info("routing, serviceId: {}, serviceUrl: {}, message size: {}", serviceId, serviceUrl, bytesList.size());
                        restTemplate.execute("http://" + serviceId + serviceUrl, HttpMethod.POST, request -> {
                            OutputStream outputStream = request.getBody();
                            try (DataOutputStream dos = new DataOutputStream(outputStream)) {
                                for (byte[] bytes : bytesList) {
                                    dos.writeInt(bytes.length);
                                    dos.write(bytes);
                                }
                            }
                        }, response -> {
                            HttpStatus statusCode = response.getStatusCode();
                            if (!statusCode.is2xxSuccessful()) {
                                dynamicRabbitRouterOutResend.resend("router-out", "retry-routing", serviceId, serviceUrl, bytesList);
                            }
                            return StreamUtils.copyToByteArray(response.getBody());
                        });
                    } catch (Exception e) {
                        logger.error("", e);
                        dynamicRabbitRouterOutResend.resend("router-out", "retry-routing", serviceId, serviceUrl, bytesList);
                    }
                }));
    }

}

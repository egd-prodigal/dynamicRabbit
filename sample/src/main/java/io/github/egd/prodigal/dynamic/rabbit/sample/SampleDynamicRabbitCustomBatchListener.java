package io.github.egd.prodigal.dynamic.rabbit.sample;

import io.github.egd.prodigal.dynamic.rabbit.listener.DynamicRabbitCustomBatchListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class SampleDynamicRabbitCustomBatchListener extends DynamicRabbitCustomBatchListenerAdapter {

    private final Logger logger = LoggerFactory.getLogger(SampleDynamicRabbitCustomBatchListener.class);

    private RestTemplate restTemplate = new RestTemplateBuilder()
            .setConnectTimeout(Duration.ofMinutes(5))
            .setReadTimeout(Duration.ofMinutes(5)).build();

    @Override
    protected List<String> supportedQueueNames() {
        List<String> list = new ArrayList<>();
        list.add("demo_queue");
        list.add("demo1");
        list.add("demo2");
        list.add("demo3");
        return list;
    }


    @Override
    public void consume(List<Message> messages) {
        // route
        String result = restTemplate.execute("http://localhost:8001/test", HttpMethod.POST, request -> {
            request.getHeaders().setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
            OutputStream outputStream = request.getBody();
            try (DataOutputStream dos = new DataOutputStream(outputStream)) {
                for (Message message : messages) {
                    byte[] body = message.getBody();
                    dos.writeInt(body.length);
                    dos.write(body);
                }
            }
        }, response -> {
            InputStream inputStream = response.getBody();
            return StreamUtils.copyToString(inputStream, Charset.defaultCharset());
        });
        System.out.println(result);
    }

}

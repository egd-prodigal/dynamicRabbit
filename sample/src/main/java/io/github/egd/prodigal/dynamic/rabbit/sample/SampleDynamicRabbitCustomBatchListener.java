package io.github.egd.prodigal.dynamic.rabbit.sample;

import com.rabbitmq.client.Channel;
import io.github.egd.prodigal.dynamic.rabbit.listener.DynamicRabbitCustomBatchListenerAdapter;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SampleDynamicRabbitCustomBatchListener extends DynamicRabbitCustomBatchListenerAdapter {

    private final Logger logger = LoggerFactory.getLogger(SampleDynamicRabbitCustomBatchListener.class);

    private final AtomicInteger count = new AtomicInteger();

    private RestTemplate restTemplate = new RestTemplateBuilder()
            .setConnectTimeout(Duration.ofMinutes(5))
            .setReadTimeout(Duration.ofMinutes(5)).build();

    @Autowired
    private ConsumeMapper consumeMapper;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

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
    public void consume(List<Message> messages, Channel channel) {
        // route
//        String result = restTemplate.execute("http://localhost:8001/test", HttpMethod.POST, request -> {
//            request.getHeaders().setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
//            OutputStream outputStream = request.getBody();
//            try (DataOutputStream dos = new DataOutputStream(outputStream)) {
//                for (Message message : messages) {
//                    byte[] body = message.getBody();
//                    dos.writeInt(body.length);
//                    dos.write(body);
//                }
//            }
//        }, response -> {
//            InputStream inputStream = response.getBody();
//            return StreamUtils.copyToString(inputStream, Charset.defaultCharset());
//        });
//        System.out.println(result);
//        try {
//            TimeUnit.SECONDS.sleep(1L);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }

        for (Message message : messages) {
            long deliveryTag = message.getMessageProperties().getDeliveryTag();
            consumeMapper.save(new String(message.getBody()));
            try {
                channel.basicAck(deliveryTag, false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        logger.info("consume count: " + count.addAndGet(messages.size()));

    }

}

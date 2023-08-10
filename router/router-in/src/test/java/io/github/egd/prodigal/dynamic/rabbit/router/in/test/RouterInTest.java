package io.github.egd.prodigal.dynamic.rabbit.router.in.test;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.stream.IntStream;

public class RouterInTest {

    @Test
    public void batchSend() {
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "256");
        RestTemplate restTemplate = new RestTemplate();
        IntStream.range(0, 10).parallel().forEach(i -> {
            ResponseEntity<Byte> responseEntity = restTemplate.postForEntity("http://localhost:8007/demo_exchange/demo_binding", "abc", Byte.class);
            Byte body = responseEntity.getBody();
            System.out.println(body);
        });
    }

}

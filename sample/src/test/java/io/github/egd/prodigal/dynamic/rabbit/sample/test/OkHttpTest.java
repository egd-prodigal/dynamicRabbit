package io.github.egd.prodigal.dynamic.rabbit.sample.test;

import okhttp3.*;
import okio.BufferedSink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class OkHttpTest {

    @Test
    public void test() throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url("http://localhost:8001/test")
                .post(new RequestBody() {
                    @Nullable
                    @Override
                    public MediaType contentType() {
                        return MediaType.get("application/octet-stream");
                    }

                    @Override
                    public void writeTo(@NotNull BufferedSink bufferedSink) throws IOException {
                        byte[] bytes = "生一鸣".getBytes(StandardCharsets.UTF_8);
                        bufferedSink.writeInt(bytes.length);
                        bufferedSink.write(bytes);
                    }
                }).build();
        Call call = okHttpClient.newCall(request);
        Response response = call.execute();
        System.out.println(response.body().string());
    }

    @Test
    public void restTest() {
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.execute("http://localhost:8001/test", HttpMethod.POST, request -> {
            request.getHeaders().setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
            OutputStream outputStream = request.getBody();
            try (DataOutputStream dos = new DataOutputStream(outputStream)) {
                byte[] bytes = "生一鸣".getBytes(StandardCharsets.UTF_8);
                dos.writeInt(bytes.length);
                dos.write(bytes);
            }
        }, response -> {
            InputStream inputStream = response.getBody();
            return StreamUtils.copyToString(inputStream, Charset.defaultCharset());
        });
        System.out.println(result);
    }

}

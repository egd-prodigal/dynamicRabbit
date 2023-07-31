package io.github.egd.prodigal.dynamic.rabbit.sample;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.serializer.Deserializer;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

@Component
public class StringDeserializer implements Deserializer<String> {

    @NotNull
    @Override
    public String deserialize(@NotNull InputStream inputStream) throws IOException {
        return StreamUtils.copyToString(inputStream, Charset.defaultCharset());
    }

    @NotNull
    @Override
    public String deserializeFromByteArray(@NotNull byte[] serialized) throws IOException {
        return new String(serialized, Charset.defaultCharset());
    }

}

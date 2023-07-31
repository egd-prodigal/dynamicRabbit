package io.github.egd.prodigal.dynamic.rabbit.router.core;

import org.springframework.core.serializer.Deserializer;
import org.springframework.lang.NonNull;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class StringDeserializer implements Deserializer<String> {

    @NonNull
    @Override
    public String deserialize(@NonNull InputStream inputStream) throws IOException {
        return StreamUtils.copyToString(inputStream, Charset.defaultCharset());
    }

    @NonNull
    @Override
    public String deserializeFromByteArray(@NonNull byte[] serialized) throws IOException {
        return new String(serialized, Charset.defaultCharset());
    }

}

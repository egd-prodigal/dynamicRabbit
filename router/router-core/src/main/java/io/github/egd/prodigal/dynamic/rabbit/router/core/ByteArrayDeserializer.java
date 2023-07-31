package io.github.egd.prodigal.dynamic.rabbit.router.core;

import org.springframework.core.serializer.Deserializer;
import org.springframework.lang.NonNull;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;

public class ByteArrayDeserializer implements Deserializer<byte[]> {

    @NonNull
    @Override
    public byte[] deserialize(@NonNull InputStream inputStream) throws IOException {
        return StreamUtils.copyToByteArray(inputStream);
    }

    @NonNull
    @Override
    public byte[] deserializeFromByteArray(@NonNull byte[] serialized) throws IOException {
        return serialized;
    }

}

package io.github.egd.prodigal.dynamic.rabbit.sample;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.serializer.Deserializer;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;

@Component
public class ByteArrayDeserializer implements Deserializer<byte[]> {

    @NotNull
    @Override
    public byte[] deserialize(@NotNull InputStream inputStream) throws IOException {
        return StreamUtils.copyToByteArray(inputStream);
    }

    @NotNull
    @Override
    public byte[] deserializeFromByteArray(@NonNull byte[] serialized) throws IOException {
        return serialized;
    }

}

package io.github.egd.prodigal.dynamic.rabbit.router.core;

import org.springframework.core.serializer.DefaultDeserializer;
import org.springframework.core.serializer.Deserializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface MessageParam {

    Class<? extends Deserializer<?>> deserializerClass() default DefaultDeserializer.class;

}

package io.github.egd.prodigal.dynamic.rabbit.router.core;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodParameter;
import org.springframework.core.serializer.Deserializer;
import org.springframework.lang.NonNull;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageParamArgumentResolver implements HandlerMethodArgumentResolver, ApplicationContextAware {

    private final Map<Class<?>, Deserializer<?>> deserializerMap = new HashMap<>();

    private ApplicationContext applicationContext;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(MessageParam.class) && List.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest httpServletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        if (httpServletRequest == null) {
            return new ArrayList<>(0);
        }
        MessageParam messageParam = parameter.getParameterAnnotation(MessageParam.class);
        if (messageParam == null) {
            return new ArrayList<>(0);
        }
        List<Object> list = new ArrayList<>();
        ServletInputStream inputStream = httpServletRequest.getInputStream();
        byte[] bytes = StreamUtils.copyToByteArray(inputStream);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        Deserializer<?> deserializer = getDeserializer(messageParam.deserializerClass());
        if (deserializer == null) {
            throw new IllegalArgumentException("unknown deserializer type");
        }
        while (byteBuffer.hasRemaining()) {
            int anInt = byteBuffer.getInt();
            byte[] dst = new byte[anInt];
            byteBuffer.get(dst);
            list.add(deserializer.deserializeFromByteArray(dst));
        }
        return list;
    }

    private Deserializer<?> getDeserializer(Class<? extends Deserializer<?>> type) {
        if (deserializerMap.containsKey(type)) {
            return deserializerMap.get(type);
        }
        synchronized (deserializerMap) {
            Deserializer<?> deserializer = applicationContext.getBean(type);
            deserializerMap.put(type, deserializer);
            return deserializer;
        }
    }


    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}

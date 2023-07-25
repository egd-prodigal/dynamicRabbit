package io.github.egd.prodigal.dynamic.rabbit.sample;

import org.springframework.context.annotation.Bean;
import org.springframework.core.MethodParameter;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class StringListMessageArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(StringMessage.class) && parameter.getParameterType().equals(StringMessageWrapper.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest httpServletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        ServletInputStream inputStream = httpServletRequest.getInputStream();
        byte[] bytes = StreamUtils.copyToByteArray(inputStream);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        List<String> list = new ArrayList<>();
        while (byteBuffer.hasRemaining()) {
            int anInt = byteBuffer.getInt();
            byte[] dst = new byte[anInt];
            byteBuffer.get(dst);
            String s = new String(dst);
            list.add(s);
        }
        return new StringMessageWrapper(bytes, list);
    }


}

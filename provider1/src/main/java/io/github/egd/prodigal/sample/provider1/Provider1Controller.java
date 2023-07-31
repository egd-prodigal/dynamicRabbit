package io.github.egd.prodigal.sample.provider1;

import io.github.egd.prodigal.dynamic.rabbit.router.core.MessageParam;
import io.github.egd.prodigal.dynamic.rabbit.router.core.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
public class Provider1Controller {

    private final Logger logger = LoggerFactory.getLogger(Provider1Controller.class);

    @RequestMapping("/demo/provider")
    public Object provider(@MessageParam(deserializerClass = StringDeserializer.class) List<String> list) {
        logger.info("list: {}", list);
        return Collections.singletonMap("code", "200");
    }

}

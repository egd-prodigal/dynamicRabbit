package io.github.egd.prodigal.dynamic.rabbit.sample;

import io.github.egd.prodigal.dynamic.rabbit.router.core.MessageParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class SampleController {

    private final Logger logger = LoggerFactory.getLogger(SampleController.class);

    @RequestMapping("/test")
    public String test(@MessageParam List<SampleMessageBean> list) throws IOException {
        logger.info("request data: {}", list);
        return "success";
    }

}

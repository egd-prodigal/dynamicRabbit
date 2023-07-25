package io.github.egd.prodigal.dynamic.rabbit.sample;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class SampleController {

    @RequestMapping("/test")
    public String test(@StringMessage StringMessageWrapper stringMessageWrapper) throws IOException {
        System.out.println(stringMessageWrapper.getList());
        return "success";
    }

}

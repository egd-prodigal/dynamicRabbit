package io.github.egd.prodigal.dynamic.rabbit.sample;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class SampleController {

    @RequestMapping("/test")
    public String test(@MessageParam List<SampleMessageBean> list) throws IOException {
        System.out.println(list);
        return "success";
    }

}

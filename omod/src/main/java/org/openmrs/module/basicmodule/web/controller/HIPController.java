package org.openmrs.module.basicmodule.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HIPController {
    @RequestMapping("/v1/hip/hello")
    public String helloWorld() {
        return "Hello World";
    }

}

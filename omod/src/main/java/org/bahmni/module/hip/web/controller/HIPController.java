package org.bahmni.module.hip.web.controller;

import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/hip")
@Controller
public class HIPController {

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value="/hello")
    public String helloWorld() {
        return "Hello World";
    }

}

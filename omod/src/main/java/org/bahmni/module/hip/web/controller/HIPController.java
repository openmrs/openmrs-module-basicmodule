package org.bahmni.module.hip.web.controller;

import com.google.gson.Gson;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/hip")
@Controller
public class HIPController {

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value="/hello", params = {
            "personUuid"
    })
    public String helloWorld(@RequestParam("personUuid") String personUuid) {
        if(personUuid.isEmpty()){
            return "UUID is empty";
        }
        Person dummyPerson = new Person();
        dummyPerson.setUuid(personUuid);
        Patient ourPatient = new Patient(dummyPerson);
        List<Order> listOrders = org.openmrs.api.context.Context.getOrderService().getAllOrdersByPatient(ourPatient);
        String ordersToJson = new Gson().toJson(listOrders);
        return ordersToJson;
    }

}

package com.sky.schedule.test.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by gantianxing on 2017/7/20.
 */
@Controller
public class MyTestController {
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home() {
        return "hello";
    }
}

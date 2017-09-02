package com.sky.schedule.server.controller;

import com.sky.schedule.server.vo.ClientVo;
import com.sky.schedule.server.vo.GroupNettyClientMap;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;

/**
 * Created by gantianxing on 2017/7/20.
 */
@Controller
public class MyTestController {
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home() {
        return "hello";
    }

    @RequestMapping(value = "/client", method = RequestMethod.GET)
    public String client(Model model){
        Map<Integer,List<ClientVo>> clientVoMap = GroupNettyClientMap.getAllClients();
        model.addAttribute("clientVoMap",clientVoMap);
        return "list";
    }
}

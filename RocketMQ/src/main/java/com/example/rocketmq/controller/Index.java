package com.example.rocketmq.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class Index {

    @RequestMapping("/")
    public String index(){
        return "index";
    }

    @RequestMapping("/seckill")
    public String seckill(){

        //直接写入消息队列，如果超过人数，页面直接挂掉


        return "";
    }

}

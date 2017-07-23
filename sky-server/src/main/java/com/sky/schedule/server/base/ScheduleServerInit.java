package com.sky.schedule.server.base;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * Created by gantianxing on 2017/7/20.
 */
@Component
public class ScheduleServerInit {

    @Value("${server.port}")
    private int port;

    @Resource
    private NettyServer bootstrap;

    /**
     * netty服务启动方法
     */
    @PostConstruct
    public void init(){
        int t = port;
        bootstrap.startServer();
    }

    /**
     * netty服务销毁方法
     */
    @PreDestroy
    public void destroy(){
        if(bootstrap != null){
            bootstrap.stopServer();
        }
    }
}

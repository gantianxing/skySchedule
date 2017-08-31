package com.sky.schedule.client;

import com.sky.schedule.client.base.ClientNode;
import com.sky.schedule.client.base.CommonUtils;
import com.sky.schedule.client.base.NettyClientService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import static com.sky.schedule.client.base.ClientNode.*;
import static com.sky.schedule.client.base.ClientConstants.*;

/**
 * Created by gantianxing on 2017/7/20.
 */
@Component
public class ScheduleClientInit {

    private static final Log log = LogFactory.getLog(ScheduleClientInit.class);

    @Resource
    private NettyClientService nettyClientService;

    @Resource
    private Environment env;

    /**
     * netty服务启动方法
     */
    @PostConstruct
    public void init(){

        initStatic();

        //建立连接
        nettyClientService.connectServers();

        //启动定时节点获取worker
        nettyClientService.taskReq();

        //启动重连worker
        nettyClientService.reConnect();
    }

    private void initStatic(){
        String ip= CommonUtils.getIp();
        if(ip==null){
            log.error("获取本机ip失败");
            System.exit(0);//强制退出
        }

        //测试时为了保证同一台机器，启动多个客户端，对客户端标识加UUID，防止重复
        //显上如果，一条机器只部署一个实例可以去掉uuid
        clientId = ip+":"+CommonUtils.getUUId();

        groupId = Integer.parseInt(env.getProperty("group.id"));

        RE_CONN_WAIT_SECONDS = Integer.parseInt(env.getProperty("RE.CONN.WAIT.SECONDS"));

        TASK_REQ_WAIT_SECONDS = Integer.parseInt(env.getProperty("TASK.REQ.WAIT.SECONDS"));

        READ_WAIT_SECONDS = Integer.parseInt(env.getProperty("READ.WAIT.SECONDS"));

        WRITE_WAIT_SECONDS = Integer.parseInt(env.getProperty("WRITE.WAIT.SECONDS"));

        username=env.getProperty("sky.user.name");

        password=env.getProperty("password");
    }

    /**
     * netty服务销毁方法
     */
    @PreDestroy
    public void destroy(){
        nettyClientService.relaseAll();
    }


}

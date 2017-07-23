package com.sky.schedule.client.base;


import com.sky.schedule.client.netty.NettyClientHandler;
import com.sky.schedule.client.netty.NettyClient;
import com.sky.schedule.client.vo.ClientInfo;
import com.sky.schedule.client.vo.NettyServerInfo;
import com.sky.schedule.common.base.BaseMsg;
import com.sky.schedule.common.common.GroupTaskMsg;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import static com.sky.schedule.client.base.ClientConstants.*;
import static com.sky.schedule.client.base.ClientNode.*;

/**
 * Created by gantianxing on 2015/12/18.
 */

@Component
public class NettyClientService {

    private static final Log log = LogFactory.getLog(NettyClientService.class);

    private ScheduledExecutorService executorReConService;

    private ScheduledExecutorService executorTaskService;

    @Resource
    private NettyClientHandler handler;

    @Resource
    private Environment env;


    /**
     * 遍历服务器列表，建立长连接
     */
    public void connectServers(){
        String serverStr = env.getProperty("server.ip.port");
        if(StringUtils.isEmpty(serverStr)){
            return;
        }
        String [] newServers = serverStr.split(",");
        for(String oneServer:newServers){
            regToServer(oneServer);
        }
    }


    /**
     * 检查服务列表,把新的服务放入重连map
     * @param serverInfo 格式serverId:serverPort
     */
    private void regToServer(String serverInfo){
        //step1 检查serverInfo是否已经在 有效的server map里
        if(NettyServerInfo.getValid(serverInfo)!=null){
            return ;
        }

        //step2与服务端建立连接
        if(NettyServerInfo.getInvalid(serverInfo) == null){
            String oneServer [] = serverInfo.split("\\:");
            String ip = oneServer[0];
            int port = Integer.valueOf(oneServer[1]).intValue();
            NettyClient nettyClient = new NettyClient(port,ip,handler);
            ClientInfo clientInfo = new ClientInfo();
            clientInfo.setRegId(clientId);
            clientInfo.setNettyClient(nettyClient);
            nettyClient.connect();

            //连接成功方有效map，否则放无效map
            if(nettyClient.channel == null){
                NettyServerInfo.addInValid(serverInfo, clientInfo);
            }else {
                NettyServerInfo.addValid(serverInfo, clientInfo);
            }

        }
    }

    /**
     * 定时重链接无效的服务器列表
     */
    public void reConnect(){
        executorReConService = Executors.newScheduledThreadPool(1);
        executorReConService.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                try {
                    Map<String, ClientInfo> invalid = NettyServerInfo.getAllInvalid();
                    for (String key : invalid.keySet()) {
                        ClientInfo clientInfo = invalid.get(key);
                        clientInfo.getNettyClient().connect();

                        //连接成功后
                        if (clientInfo.getNettyClient().channel != null) {
                            NettyServerInfo.removeInvalid(key);
                            NettyServerInfo.addValid(key, clientInfo);
                        }
                    }
                }catch (Exception e){
                    log.error("定时重连失败",e);
                }
            }
        }, 0, RE_CONN_WAIT_SECONDS, TimeUnit.SECONDS);
    }


    /**
     * 定时随机取一台有效的服务器发起请求
     */
    public void taskReq() {
        executorTaskService = Executors.newScheduledThreadPool(1);
        executorTaskService.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                try{
                    ClientInfo jshopClient = NettyServerInfo.getRandomChannl();
                    if(jshopClient!=null){
                        NettyClient nettyClient = jshopClient.getNettyClient();
                        if (nettyClient.channel != null) {
                            log.info("random send task.server address:"+nettyClient.channel.remoteAddress());
                            GroupTaskMsg taskMsg = new GroupTaskMsg();
                            taskMsg.setGroupId(groupId);
                            taskMsg.setClientId(clientId);
                            nettyClient.channel.writeAndFlush(taskMsg);
                        }
                    }else
                    {
                        log.error("没有可用的服务");
                    }

                } catch (Exception e){
                    log.error("定时发起task任务失败",e);
                }


            }
        }, TASK_REQ_WAIT_SECONDS, TASK_REQ_WAIT_SECONDS, TimeUnit.SECONDS);
    }


    /**
     * 释放资源
     */
    public void relaseAll(){
        Map<String,ClientInfo> valid = NettyServerInfo.getAllValid();
        for(String key: valid.keySet()){
            ClientInfo clientInfo = valid.get(key);
            if(clientInfo.getNettyClient().channel !=null){
                clientInfo.getNettyClient().releaseConnect();
            }
        }

        Map<String,ClientInfo> invalid = NettyServerInfo.getAllInvalid();
        for(String key: invalid.keySet()){
            ClientInfo clientInfo = invalid.get(key);
            if(clientInfo.getNettyClient().channel !=null){
                clientInfo.getNettyClient().releaseConnect();
            }
        }
    }

}

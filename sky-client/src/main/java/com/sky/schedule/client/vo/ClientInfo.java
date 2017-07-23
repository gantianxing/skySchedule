package com.sky.schedule.client.vo;


import com.sky.schedule.client.netty.NettyClient;

/**
 * Created by gantianxing on 2015/12/18.
 */
public class ClientInfo {

    /**
     * 注册id
     */
    private String regId;

    /**
     * 管道
     */
    private NettyClient nettyClient;

    public String getRegId() {
        return regId;
    }

    public void setRegId(String regId) {
        this.regId = regId;
    }

    public NettyClient getNettyClient() {
        return nettyClient;
    }

    public void setNettyClient(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }
}

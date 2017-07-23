package com.sky.schedule.common.base;

import java.io.Serializable;

/**
 * 消息基类
 * Created by gantianxing on 2015/12/15.
 */
//必须实现序列,serialVersionUID 一定要有,否者在netty消息序列化反序列化会有问题，接收不到消息！！！
public abstract class BaseMsg implements Serializable {
    private static final long serialVersionUID = 1L;

    private MsgType type;

    //客户端分组id，必须唯一
    private Integer groupId;

    //必须唯一，否者会出现channel调用混乱
    private String clientId;
    //消息id
    private long msgId;
    public long getMsgId() {
        return msgId;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public MsgType getType() {
        return type;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public void setType(MsgType type) {
        this.type = type;
    }
}

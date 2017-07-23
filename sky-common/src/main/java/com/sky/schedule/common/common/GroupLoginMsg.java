package com.sky.schedule.common.common;


import com.sky.schedule.common.base.BaseMsg;
import com.sky.schedule.common.base.MsgType;

/**
 * 集群管理类型的链接消息
 * Created by zengqijuan on 2017/3/29.
 */
public class GroupLoginMsg extends BaseMsg {

    private String userName;
    private String password;

    public GroupLoginMsg() {
        setType(MsgType.GROUP_LOGIN);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

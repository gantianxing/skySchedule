package com.sky.schedule.client.base;

import org.springframework.stereotype.Component;

/**
 * Created by gantianxing on 2017/7/21.
 */
public class ClientConstants {

    //当前节点客户group id
    public static int groupId;

    // 隔N秒后重连
    public static int RE_CONN_WAIT_SECONDS;

    // 隔N秒后向服务端发起请求
    public static int TASK_REQ_WAIT_SECONDS;

    // 25秒没有收到服务器返回，断开链接，放到重连map
    public static int READ_WAIT_SECONDS;

    // 如果空闲20秒发送一次ping信息
    public static int WRITE_WAIT_SECONDS;

    public static String username;

    public static String password;
}

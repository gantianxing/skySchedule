package com.sky.schedule.common.common;

import com.sky.schedule.common.base.BaseMsg;
import com.sky.schedule.common.base.MsgType;

/**
 * Created by gantianxing on 2015/12/21.
 */
public class PingMsg extends BaseMsg {

    public PingMsg() {
        setType(MsgType.PING);
    }
}

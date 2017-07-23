package com.sky.schedule.client.base;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.InetAddress;
import java.util.UUID;

/**
 * Created by gantianxing on 2017/7/21.
 */
public class CommonUtils {
    private static final Log log = LogFactory.getLog(CommonUtils.class);

    /**
     * 获取本机ip
     * @return
     */
    public static String getIp() {
        String localip= null;
        try {
            InetAddress addr = InetAddress.getLocalHost();
            localip=addr.getHostAddress();
        } catch (Exception e) {
            log.error("获取本机ip失败",e);
        }
        return localip;
    }

    public static int getUUId() {
        int hashCodeV = UUID.randomUUID().toString().hashCode();
        if(hashCodeV < 0) {//有可能是负数
            hashCodeV = - hashCodeV;
        }
        return hashCodeV;
    }
}

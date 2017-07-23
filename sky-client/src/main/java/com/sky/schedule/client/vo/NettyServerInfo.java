package com.sky.schedule.client.vo;

import io.netty.channel.Channel;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对服务器链接引用的Map key=serverId:serverPort
 * Created by gantianxing on 2015/12/15.
 */
public class NettyServerInfo {

    /**
     * 有效的服务器端连接
     */
    private static Map<String,ClientInfo> validMap=new ConcurrentHashMap<String, ClientInfo>();

    /**
     * 无效的服务器链接，需定时重新链接
     */
    private static Map<String,ClientInfo> invalidMap=new ConcurrentHashMap<String, ClientInfo>();

    /**
     * 添加有效的服务器连接
     * @param serverInfo
     * @param socketChannel
     */
    public static void addValid(String serverInfo,ClientInfo socketChannel){
        for (Map.Entry<String,ClientInfo> entry:validMap.entrySet()){
            if(entry.getKey().equals(serverInfo)){
                return;
            }
        }
        validMap.put(serverInfo,socketChannel);
    }

    /**
     * 取指定的有效的服务器链接
     * @param serverInfo
     * @return
     */
    public static ClientInfo getValid(String serverInfo){
        if(validMap.size() > 0){
            for (Map.Entry<String,ClientInfo> entry:validMap.entrySet()){
                if(entry.getKey().equals(serverInfo)){
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 随机取一个有效的服务器链接
     * @return
     */
    public static ClientInfo getRandomChannl(){
        ClientInfo randomValue =null;
        Set<String> keys = validMap.keySet();
        if(keys !=null && keys.size() >0){
            String[] keyArray = keys.toArray(new String[keys.size()]);
            Random random = new Random();
            String randomKey = keyArray[random.nextInt(keyArray.length)];
            randomValue = validMap.get(randomKey);
        }
        return randomValue;
    }

    public static Map<String,ClientInfo> getAllValid(){
        return validMap;
    }

    /**
     * 从有效的服务器链接中移除
     * @param serverInfo
     */
    public static void removeValid(String serverInfo){
        for (Map.Entry<String,ClientInfo> entry:validMap.entrySet()){
            if(entry.getKey().equals(serverInfo)){
                validMap.remove(serverInfo);
            }
        }
    }

    /**
     * 根据连接删除
     * @param channel
     * @return
     */
    public static Map.Entry<String,ClientInfo> remove(Channel channel){
        Map.Entry<String,ClientInfo> ret = null;
        ClientInfo clientInfo = null;
        for (Map.Entry<String,ClientInfo> entry:validMap.entrySet()){
            clientInfo = entry.getValue();
            if (clientInfo.getNettyClient().channel == channel){
                validMap.remove(entry.getKey());
                ret = entry;
                break;
            }
        }
        return ret;
    }

    /**
     * 添加无效的服务器连接
     * @param serverInfo
     */
    public static void addInValid(String serverInfo,ClientInfo socketChannel){
        for (Map.Entry<String,ClientInfo> entry:invalidMap.entrySet()){
            if(entry.getKey().equals(serverInfo)){
                return;
            }
        }
        invalidMap.put(serverInfo,socketChannel);
    }

    /**
     * 取指定的无效的服务器链接
     * @param serverInfo
     * @return
     */
    public static ClientInfo getInvalid(String serverInfo){
        for (Map.Entry<String,ClientInfo> entry:invalidMap.entrySet()){
            if(entry.getKey().equals(serverInfo)){
                return invalidMap.get(serverInfo);
            }
        }
        return null;
    }

    /**
     * 取所有的无效的服务器链接
     * @return
     */
    public static Map<String,ClientInfo> getAllInvalid(){
        return invalidMap;
    }

    /**
     * 移除无效的服务器链接
     * @param serverInfo
     */
    public static void removeInvalid(String serverInfo){
        for (Map.Entry<String,ClientInfo> entry:invalidMap.entrySet()){
            if(entry.getKey().equals(serverInfo)){
                invalidMap.remove(serverInfo);
            }
        }
    }



}
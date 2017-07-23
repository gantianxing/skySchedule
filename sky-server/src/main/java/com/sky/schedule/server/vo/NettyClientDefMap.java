package com.sky.schedule.server.vo;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对SocketChannel引用的Map
 * Created by gantianxing on 2015/12/15.
 */
public class NettyClientDefMap {

    //客户端连接
    private static Map<String,SocketChannel> map=new ConcurrentHashMap<String, SocketChannel>();

    public static void add(String clientId,SocketChannel socketChannel){
        map.put(clientId,socketChannel);
    }

    public static Channel get(String clientId){
        return map.get(clientId);
    }

    /**
     * 根据channel查找
     * @param channel
     * @return
     */
    public static Channel get(Channel channel){
        for (Map.Entry<String,SocketChannel> entry:map.entrySet()){
            if (entry.getValue()==channel){
                return entry.getValue();
            }
        }
        return  null;
    }

    public static void remove(SocketChannel socketChannel){
        for (Map.Entry entry:map.entrySet()){
            if (entry.getValue()==socketChannel){
                map.remove(entry.getKey());
            }
        }
    }

}
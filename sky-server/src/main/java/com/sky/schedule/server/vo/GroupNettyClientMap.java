package com.sky.schedule.server.vo;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 集群链接的分组管理Map
 * Created by zengqijuan on 2017/3/30.
 */
public class GroupNettyClientMap {

    //客户端处理任务标记
    private static Map<Integer,Map<String,Integer>> groupClientMap = new ConcurrentHashMap<Integer,Map<String,Integer>>();

    //groupId分组客户端连接
    private static Map<Integer,Map<String,SocketChannel>> groupMap = new ConcurrentHashMap<Integer,Map<String,SocketChannel>>();

    private static final ReentrantLock lock = new ReentrantLock();


    /**
     * 根据groupId，增加对应map的channel,通过加锁实现：一次只允许放入一个客户端
     * @param groupId
     * @param clientId
     * @param socketChannel
     */
    public static void add(Integer groupId,String clientId,SocketChannel socketChannel){

        //根据分组获取链接map
        Map<String,SocketChannel> channelMap = groupMap.get(groupId);
        if(channelMap == null){
            lock.lock(); //加锁防止覆盖
            try{
                if(groupMap.get(groupId) == null){
                    channelMap = new ConcurrentHashMap<String, SocketChannel>();
                    groupMap.put(groupId,channelMap);
                }
            }finally {
                lock.unlock();
            }
        }
        channelMap.put(clientId,socketChannel);
    }


    /**
     * 根据groupid获取对应的socket map
     * @param groupId
     * @return
     */
    public static Map<String,SocketChannel> get(Integer groupId){
        return groupMap.get(groupId);
    }


    /**
     * 根据channel获取groupId
     * @param channel
     * @return
     */
    public static Integer getGroupId(Channel channel){
        for(Map.Entry<Integer,Map<String,SocketChannel>> entry:groupMap.entrySet()){
            Map<String,SocketChannel> tempMap = entry.getValue();
            for (Map.Entry cEntry:tempMap.entrySet()){
                if (cEntry.getValue()==channel){
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    /**
     * 根据channel获取clientid
     * @param channel
     * @return
     */
    public static String getClientId(Channel channel){
        for(Map.Entry<Integer,Map<String,SocketChannel>> entry:groupMap.entrySet()){
            Map<String,SocketChannel> tempMap = entry.getValue();
            for (Map.Entry cEntry:tempMap.entrySet()){
                if (cEntry.getValue()==channel){
                    return cEntry.getKey().toString();
                }
            }
        }
        return null;
    }

    /**
     * 根据groupId随机获取channel
     * @param groupId
     * @return
     */
    public static Channel getRandomChannel(Integer groupId,Channel channel){
        Map<String,SocketChannel> tempMap = get(groupId);
        Integer size = tempMap.size();
        if(size == 0){
            return null;
        }
        Integer randomInt =  new Random().nextInt(size) ;
        Integer flag = 0;
        Channel retChannel = null;

        for (Map.Entry<String,SocketChannel> cEntry:tempMap.entrySet()){
            flag++;
            if(cEntry.getValue()==channel){
                continue;
            }
            retChannel = cEntry.getValue();
            if(flag == randomInt || flag > randomInt){
                return retChannel;
            }
        }
        return null;
    }


    /**
     * 移除对象中的channel
     * @param channel
     */
    public static void remove(Channel channel){
        for(Map.Entry<Integer,Map<String,SocketChannel>> entry:groupMap.entrySet()){
            Map<String,SocketChannel> tempMap = entry.getValue();

            int flag = 0;
            for (Map.Entry cEntry:tempMap.entrySet()){
                if (cEntry.getValue()==channel){
                    tempMap.remove(cEntry.getKey());
                    flag = 1;
                }
            }
            //若删除则从新计算节点
            if(flag == 1){
                reLoadClientList(entry.getKey());
                return;
            }
        }
    }

    public static void main(String[] args) {
        SocketChannel sc = new NioSocketChannel();
        SocketChannel sc2 = new NioSocketChannel();
        add(1, "123", sc);
        add(1, "laji", sc2);
        System.out.println(groupMap.get(1).size());
        remove(sc);
        System.out.println(groupMap.get(1).size());

    }


    /**
     * 根据groupid和clientId获取channel
     * @param groupId
     * @param clientId
     * @return
     */
    public static Channel get(Integer groupId,String clientId){
        //根据分组获取链接map
        Map<String,SocketChannel> channelMap = groupMap.get(groupId);
        if(channelMap == null){
            return null;
        }
        return channelMap.get(clientId);
    }

    /**
     * 根据clientId获取channel
     * @param clientId
     * @return
     */
    public static Channel get(String clientId){
        for(Map.Entry<Integer,Map<String,SocketChannel>> entry:groupMap.entrySet()) {
            Map<String, SocketChannel> tempMap = entry.getValue();
            if(tempMap.get(clientId) != null){
                return tempMap.get(clientId);
            }
        }
        return null;
    }

    /**
     * 重新计算客户端任务标记
     * @param groupId
     */
    public static void reLoadClientList(Integer groupId){
        //根据分组获取链接map
        Map<String,SocketChannel> channelMap = groupMap.get(groupId);
        Map tempMap = new ConcurrentHashMap<String, Integer>();
        if(channelMap != null) {
            synchronized(channelMap){//对每个分组map对象加锁，一个分组一次只允许一次重新计算
                Set<String> clients = channelMap.keySet();
                List<String> clientList = new ArrayList<String>(clients);
                for(int index = 0;index < clientList.size();index++){
                    String clientId = clientList.get(index);
                    tempMap.put(clientId,index);
                }
                groupClientMap.put(groupId,tempMap);
            }
        }

    }

    /**
     * 获取在该groupId中的，对应clientId所在的分组编号
     * @param groupId
     * @param clientId
     * @return
     */
    public static Integer getNode(Integer groupId,String clientId){
        //根据分组获取链接map
        Map<String,Integer> clientMap = groupClientMap.get(groupId);
        if(clientMap == null){
            return null;
        }
        return clientMap.get(clientId);
    }

    /**
     * 获取在该groupId中的节点总数
     * @param groupId
     * @return
     */
    public static int getSize(Integer groupId){
        if(CollectionUtils.isEmpty(groupClientMap)||groupId==null){
            return 0;
        }
        //根据分组获取链接map
        Map<String,Integer> clientMap = groupClientMap.get(groupId);
        if(clientMap == null){
            return 0;
        }
        return clientMap.size();
    }

    /**
     * 获取所有客户端信息
     * @return
     */
    public static Map<Integer,List<ClientVo>> getAllClients() {

        Map<Integer,List<ClientVo>> clientVoMap = new LinkedHashMap<>();

        for(Integer group:groupClientMap.keySet()){
            Map<String,Integer> oneMap = groupClientMap.get(group);
            List<ClientVo> oneList = new LinkedList<>();
            for(String id:oneMap.keySet()){
                ClientVo clientVo = new ClientVo();
                clientVo.setId(id);
                clientVo.setNodeNum(oneMap.get(id));
                oneList.add(clientVo);
            }
            clientVoMap.put(group,oneList);
        }

        return clientVoMap;
    }
}

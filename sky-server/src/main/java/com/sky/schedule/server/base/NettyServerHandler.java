package com.sky.schedule.server.base;

import com.sky.schedule.common.base.BaseMsg;
import com.sky.schedule.common.base.MsgType;
import com.sky.schedule.common.common.*;
import com.sky.schedule.server.vo.GroupNettyClientMap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 服务器端handler实现
 * Created by gantianxing on 2015/12/15.
 */
@Component
@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<BaseMsg> {

    @Value("${sky.user.name}")
    private String username;

    @Value("${password}")
    private String password;

    private static final Log log = LogFactory.getLog(NettyServerHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, BaseMsg baseMsg) throws Exception {
         //登陆处理
        if(MsgType.GROUP_LOGIN.equals(baseMsg.getType())){
            GroupLoginMsg groupLoginMsg = (GroupLoginMsg)baseMsg;
            Integer groupId = groupLoginMsg.getGroupId();

            if(GroupNettyClientMap.get(groupId,groupLoginMsg.getClientId()) != null){
                log.info("group:"+groupId+",client:"+groupLoginMsg.getClientId()+" 已经登录");
                return;
            }
            if(username.equals(groupLoginMsg.getUserName())&&password.equals(groupLoginMsg.getPassword())) {
                //登录成功,把channel存到服务端group对应的map中,重新给客户端分配任务
                GroupNettyClientMap.add(groupId,groupLoginMsg.getClientId(),
                        (SocketChannel) channelHandlerContext.channel());

                //重新计算任务节点
                GroupNettyClientMap.reLoadClientList(groupId);
                log.info("group:"+groupId+",client:"+groupLoginMsg.getClientId()+" 登录成功");
            }else{//登陆失败 强制关闭
                log.info("group:"+groupId+",user name:" + groupLoginMsg.getUserName() + ",password:"
                        + groupLoginMsg.getPassword() + " login failed");
                channelHandlerContext.channel().close();
            }
            log.info("groupId:"+groupId+",size"+GroupNettyClientMap.getSize(groupId));
        }else {
            //这里强制转换会报错不
            //如果不是登陆请求，判断客户端列表中是否有该连接，如果没有强制关闭。
            if(!MsgType.GROUP_LOGIN.equals(baseMsg.getType())){
                if(GroupNettyClientMap.get(baseMsg.getClientId())==null){
                    log.error("client" + baseMsg.getClientId() + "为非法客户端，强制关闭");
                    channelHandlerContext.channel().close();
                }
            }
        }

        //客户端心跳请求
        if(MsgType.PING.equals(baseMsg.getType())){
            PingMsg pingMsg=(PingMsg)baseMsg;
            Integer groupId = pingMsg.getGroupId();
            String clientId = pingMsg.getClientId();


            //向客户端返回接收到ping信息
            InfoMsg reply=new InfoMsg();
            reply.setGroupId(groupId);
            reply.setClientId(clientId);
            reply.setInfo("server return ping");

            Channel clientChannel = GroupNettyClientMap.get(groupId,clientId);
            if (clientChannel != null){
                clientChannel.writeAndFlush(reply);
            }
        }

        //客户端任务请求
        if(MsgType.GROUP_TASK.equals(baseMsg.getType())){
            GroupTaskMsg groupTaskMsg=(GroupTaskMsg)baseMsg;
            Integer groupId = groupTaskMsg.getGroupId();
            String clientId = groupTaskMsg.getClientId();
            groupTaskMsg.setTotalNode(GroupNettyClientMap.getSize(groupId));
            groupTaskMsg.setNodeNum(GroupNettyClientMap.getNode(groupId,clientId));
            log.info("return task info groupId:"+groupId+",total:"+GroupNettyClientMap.getSize(groupId)
                    +",nodeNum:"+GroupNettyClientMap.getNode(groupId,clientId));
            GroupNettyClientMap.get(groupId,clientId).writeAndFlush(groupTaskMsg);
        }

       ReferenceCountUtil.release(baseMsg);
    }

    /**
     * 25秒没有读取到信息，说明没有客户端链接到该服务器，打印消息
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
            throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                log.info("no meg received in 25s");
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {


        StackTraceElement[] stackElements = cause.getStackTrace();//通过Throwable获得堆栈信息
        if (stackElements != null) {
            for (int i = 0; i < stackElements.length; i++) {
                log.error(stackElements[i].getFileName()+ "-"
                        +stackElements[i].getLineNumber()+ "-"
                        +stackElements[i].getMethodName());
            }
        }

        //获取客户端id
        String clientid = GroupNettyClientMap.getClientId(ctx.channel());
        log.error("连接异常，客户端信息:" + clientid);
        if(StringUtils.isNotBlank(clientid)){
            //channel失效，从Map中移除
            GroupNettyClientMap.remove(ctx.channel());
            //重新计算任务节点
            Integer groupId = GroupNettyClientMap.getGroupId(ctx.channel());
            GroupNettyClientMap.reLoadClientList(groupId);
        }
        ctx.channel().close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("新节点加入");
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        //获取客户端id
        String clientid = GroupNettyClientMap.getClientId(ctx.channel());
        log.error("客户端:clientid出现故障,SkySchedule开始重新分配任务");
        if(StringUtils.isNotBlank(clientid)){
            //channel失效，从Map中移除
            GroupNettyClientMap.remove(ctx.channel());

            //重新计算任务节点
            Integer groupId = GroupNettyClientMap.getGroupId(ctx.channel());
            GroupNettyClientMap.reLoadClientList(groupId);
        }
        ctx.channel().close();
    }

}

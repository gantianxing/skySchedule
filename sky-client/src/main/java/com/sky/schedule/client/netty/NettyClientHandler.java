package com.sky.schedule.client.netty;

/**
 * Created by gantianxing on 2015/12/18.
 */

import com.sky.schedule.client.base.ClientNode;
import com.sky.schedule.client.vo.ClientInfo;
import com.sky.schedule.client.vo.NettyServerInfo;
import com.sky.schedule.common.base.BaseMsg;
import com.sky.schedule.common.base.MsgType;
import com.sky.schedule.common.common.GroupTaskMsg;
import com.sky.schedule.common.common.PingMsg;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import static com.sky.schedule.client.base.ClientConstants.*;
import static com.sky.schedule.client.base.ClientNode.*;


import java.util.Map;

/**
 * 客户端handler
 * Created by gantianxing on 2015/12/15.
 */
@Component
@ChannelHandler.Sharable
public class NettyClientHandler extends SimpleChannelInboundHandler<BaseMsg> {

    private static final Log log = LogFactory.getLog(NettyClientHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, BaseMsg baseMsg) throws Exception {

//        if(MsgType.INFO.equals(baseMsg.getType())){
//            InfoMsg infoMsg = (InfoMsg) baseMsg;
//            log.info("received msg:" + infoMsg.getInfo()+"server address:"+channelHandlerContext.channel().remoteAddress());
//        }

        //接收到服务器发布的任务
        if(MsgType.GROUP_TASK.equals(baseMsg.getType())){
            GroupTaskMsg taskMsg = (GroupTaskMsg)baseMsg;
            if(ClientNode.clientId.equals(taskMsg.getClientId())){
                Integer totalNode = taskMsg.getTotalNode();//总节点数
                Integer nodeNum = taskMsg.getNodeNum();//当前节点数
                if(totalNode!=null && totalNode >0 && nodeNum != null){
                    ClientNode.totalNode = totalNode;
                    ClientNode.nodeNum = nodeNum;
                    //log.info("received new task,totalNode:"+totalNode+" nodeNum:"+nodeNum+"server address:"+channelHandlerContext.channel().remoteAddress());
                }else {
                    log.error("error received wrong task1,please check----------"+"server address:"+channelHandlerContext.channel().remoteAddress());
                }
            }else {
                log.error("error received wrong task2,please check----------"+"server address:"+channelHandlerContext.channel().remoteAddress());
            }
        }

        ReferenceCountUtil.release(baseMsg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("channel exceptionCaught,error reason:" + cause.getMessage()+"server address:"+ctx.channel().remoteAddress());

        Map.Entry<String,ClientInfo> server = NettyServerInfo.remove(ctx.channel());
        if(server!=null && NettyServerInfo.getInvalid(server.getKey()) == null){
            NettyServerInfo.addInValid(server.getKey(), server.getValue());
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("channel channelActive,Client active.server address:"+ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("channel channelInactive.server address:"+ctx.channel().remoteAddress());
        Map.Entry<String,ClientInfo> server = NettyServerInfo.remove(ctx.channel());
        if(server!=null && NettyServerInfo.getInvalid(server.getKey()) == null){
            NettyServerInfo.addInValid(server.getKey(), server.getValue());
        }
    }

    /**
     * 检查指定时间内有没有收到服务器端返回
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;

            //读空闲25秒从有效中删除，加入重连列表
            if (event.state() == IdleState.READER_IDLE) {
	                /*读超时*/
                //log.info("read idle,remove connect.server address:"+ctx.channel().remoteAddress());
                Map.Entry<String,ClientInfo> server = NettyServerInfo.remove(ctx.channel());
                if(NettyServerInfo.getInvalid(server.getKey()) == null){
                    NettyServerInfo.addInValid(server.getKey(),server.getValue());
                }
            }

            //写空闲20秒向服务端发送，一次ping请求，保持长连接
            if(event.state() == IdleState.WRITER_IDLE){
                PingMsg pingMsg = new PingMsg();
                pingMsg.setGroupId(groupId);
                pingMsg.setClientId(clientId);
                //log.info("write idle,send ping.server address:"+ctx.channel().remoteAddress());
                ctx.channel().writeAndFlush(pingMsg);
            }
        }
    }

}

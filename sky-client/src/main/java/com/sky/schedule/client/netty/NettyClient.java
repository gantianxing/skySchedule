package com.sky.schedule.client.netty;

import com.sky.schedule.client.base.ClientNode;
import com.sky.schedule.common.common.GroupLoginMsg;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static com.sky.schedule.client.base.ClientConstants.*;
import static com.sky.schedule.client.base.ClientNode.*;

import java.util.concurrent.TimeUnit;

/**
 * 客户端
 * Created by gantianxing on 2015/12/15.
 */
public class NettyClient {
    private static final Log log = LogFactory.getLog(NettyClient.class);
    private int port;
    private String host;
    private EventLoopGroup group;
    Bootstrap bootstrap;
    public Channel channel;

    private NettyClientHandler businessHander;

    public NettyClient(int port, String host, NettyClientHandler businessHander){
        this.host = host ;
        this.port = port ;
        this.businessHander = businessHander;
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.group(group);
        bootstrap.handler(new ChildChannelHandler());
    }

    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            socketChannel.pipeline().addLast(new ObjectEncoder());
            socketChannel.pipeline().addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
            socketChannel.pipeline().addLast(new IdleStateHandler(READ_WAIT_SECONDS, WRITE_WAIT_SECONDS, 0, TimeUnit.SECONDS));
            socketChannel.pipeline().addLast(businessHander);
        }
    }

    public void connect() {
        try {
            // 连接服务端
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            ChannelFuture future = bootstrap.connect(host, port).sync();
            channel = future.channel();

            // 此方法会阻塞
            //channel.closeFuture().sync();
            if (future.isSuccess()) {
                log.info("connect server success-" + channel.remoteAddress());

                //登陆服务器
                GroupLoginMsg loginMsg = new GroupLoginMsg();
                loginMsg.setClientId(clientId);
                loginMsg.setPassword(password);
                loginMsg.setUserName(username);
                loginMsg.setGroupId(groupId);
                channel.writeAndFlush(loginMsg);
                log.info("login,server address-" + channel.remoteAddress());
            }

        } catch (Exception e) {
            log.error("connect server failed-"+host+":"+port);
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            channel = null;
        }
    }

    /**
     * 断开链接，释放资源
     */
    public void releaseConnect(){
        if(channel!=null&&channel.isOpen()){
            log.info("releaseConnect,server address:" + channel.remoteAddress());
            channel.close();
        }
        if(group!=null){
            group.shutdownGracefully();
        }

    }

}

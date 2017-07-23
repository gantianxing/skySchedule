package com.sky.schedule.server.base;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 服务器端启动
 * Created by gantianxing on 2015/12/15.
 */
@Component
public class NettyServer {
    private static final Log log = LogFactory.getLog(NettyServer.class);
    private EventLoopGroup boss;
    private EventLoopGroup worker;

    @Value("${server.port}")
    private int port;

    @Resource
    private SimpleChannelInboundHandler businessHander;
    ChannelFuture channelFuture ;
    ServerBootstrap bootstrap ;

    //25秒没有接收到客户端请求，客户端可能已经挂掉。删除该客户端连接
    @Value("${read.wait.seconds}")
    private int READ_WAIT_SECONDS;

    public NettyServer(){
        this.boss=new NioEventLoopGroup();
        this.worker=new NioEventLoopGroup();
    }

    public void startServer() {
        try {
            bootstrap=new ServerBootstrap();
            bootstrap.group(boss,worker);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.option(ChannelOption.SO_BACKLOG, 128);
            //通过NoDelay禁用Nagle,使消息立即发出去，不用等待到一定的数据量才发出去
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            //保持长连接状态
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.childHandler(new ChildChannelHandler(this.businessHander));
            channelFuture = bootstrap.bind(port).sync();
            if(channelFuture.isSuccess()){
                log.info("netty server start---------------");
            }
        } catch (InterruptedException e) {
            log.error("netty server start failed",e);
        }

    }

    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
        private SimpleChannelInboundHandler businessHander;

        public ChildChannelHandler(SimpleChannelInboundHandler businessHander){
            this.businessHander = businessHander;
        }

        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            ChannelPipeline pipeline = socketChannel.pipeline();
            //读监听
            pipeline.addLast(new IdleStateHandler(READ_WAIT_SECONDS, 0, 0));
                    /*
                     * 使用ObjectDecoder和ObjectEncoder
                     * 因为双向都有写数据和读数据，所以这里需要两个都设置
                     * 如果只读，那么只需要ObjectDecoder即可
                     */
            pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(this.getClass().getClassLoader())));
            pipeline.addLast(new ObjectEncoder());
            pipeline.addLast(businessHander);
        }
    }


    /**
     * 停止服务，释放资源
     */
    public void stopServer(){
        if(channelFuture!=null){
            Channel channel = channelFuture.channel();
            if(channel!=null && channel.isOpen()){
                channel.close();
            }
        }
        if(boss!=null){
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
        log.info("netty server stop");
    }

}

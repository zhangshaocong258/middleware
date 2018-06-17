package com.alibaba.dubbo.performance.demo.agent.agent.provider;
/**
 * Created by zsc on 2018/5/16.
 */

import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;

public class ProviderServer {
    private static EtcdRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));
    public void bind(int port) throws Exception {
        EventLoopGroup bossGroup = new EpollEventLoopGroup(2);
        EventLoopGroup workerGroup = new EpollEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workerGroup)
                    .channel(EpollServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,1028)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    .childHandler(new ProviderServerInitializer());
            ChannelFuture channelFuture = serverBootstrap.bind("0.0.0.0",port).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}

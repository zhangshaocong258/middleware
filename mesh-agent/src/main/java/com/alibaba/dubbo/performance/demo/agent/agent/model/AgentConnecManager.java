package com.alibaba.dubbo.performance.demo.agent.agent.model;

import com.alibaba.dubbo.performance.demo.agent.agent.consumer.ConsumerClientInitializer;
import com.alibaba.dubbo.performance.demo.agent.agent.consumer.ConsumerServerInitializer;
import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClientInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Created by zsc on 2018/5/17.
 */
public class AgentConnecManager {
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);

    private Bootstrap bootstrap;

    private Channel channel;
    private Object lock = new Object();

    public AgentConnecManager() {
    }

    public Channel getChannel() throws Exception {
        if (null != channel) {
            return channel;
        }

        if (null == bootstrap) {
            synchronized (lock) {
                if (null == bootstrap) {
                    initBootstrap();
                }
            }
        }

        if (null == channel) {
            synchronized (lock){
                if (null == channel){
                    int port = Integer.valueOf(System.getProperty("dubbo.protocol.port"));
                    channel = bootstrap.connect("127.0.0.1", port).sync().channel();
                }
            }
        }

        return channel;
    }

    public void initBootstrap() {
        bootstrap = new Bootstrap();
        bootstrap.group(channel.eventLoop())
                .channel(EpollSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new ConsumerClientInitializer());
    }

}

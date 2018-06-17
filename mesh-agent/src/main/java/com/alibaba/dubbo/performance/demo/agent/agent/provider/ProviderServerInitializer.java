package com.alibaba.dubbo.performance.demo.agent.agent.provider;

import com.alibaba.dubbo.performance.demo.agent.agent.model.KryoPoolFactory;
import com.alibaba.dubbo.performance.demo.agent.agent.model.MessageDecoder;
import com.alibaba.dubbo.performance.demo.agent.agent.model.MessageEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * Created by zsc on 2018/5/17.
 */
public class ProviderServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel SocketChannel) throws Exception {
        SocketChannel.pipeline()
                .addLast(new MessageEncoder(KryoPoolFactory.getKryoPoolInstance()))
                .addLast(new MessageDecoder(KryoPoolFactory.getKryoPoolInstance()))
                .addLast(new ProviderServerHandler());
    }
}

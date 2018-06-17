package com.alibaba.dubbo.performance.demo.agent.agent.consumer;

import com.alibaba.dubbo.performance.demo.agent.agent.model.KryoPoolFactory;
import com.alibaba.dubbo.performance.demo.agent.agent.model.MessageDecoder;
import com.alibaba.dubbo.performance.demo.agent.agent.model.MessageEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * Created by zsc on 2018/5/17.
 */
public class ConsumerClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                .addLast(new MessageEncoder(KryoPoolFactory.getKryoPoolInstance()))
                .addLast(new MessageDecoder(KryoPoolFactory.getKryoPoolInstance()))
                .addLast(new ConsumerClientHandler());

    }
}

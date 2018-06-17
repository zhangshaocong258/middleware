package com.alibaba.dubbo.performance.demo.agent.agent.consumer;

import com.alibaba.dubbo.performance.demo.agent.agent.model.AgentFuture;
import com.alibaba.dubbo.performance.demo.agent.agent.model.Holder;
import com.alibaba.dubbo.performance.demo.agent.agent.model.MessageResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zsc on 2018/5/17.
 */
public class ConsumerClientHandler extends SimpleChannelInboundHandler<MessageResponse> {
    private Logger logger = LoggerFactory.getLogger(ConsumerClientHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessageResponse messageResponse) throws Exception {
        AgentFuture<MessageResponse> future = Holder.removeRequest(messageResponse.getMessageId());
        if (future != null) {
            future.done(messageResponse);
            logger.info("cccccccffffffffuturedone");

        }
    }
}

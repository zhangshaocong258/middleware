package com.alibaba.dubbo.performance.demo.agent.agent.consumer;

import com.alibaba.dubbo.performance.demo.agent.agent.model.AgentFuture;
import com.alibaba.dubbo.performance.demo.agent.agent.model.Holder;
import com.alibaba.dubbo.performance.demo.agent.agent.model.MessageRequest;
import com.alibaba.dubbo.performance.demo.agent.agent.model.MessageResponse;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.util.IdGenerator;
import com.alibaba.dubbo.performance.demo.agent.util.LoadBalance;
import com.alibaba.dubbo.performance.demo.agent.util.RequestParser;
import com.alibaba.dubbo.performance.demo.agent.util.WaitService;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

/**
 * Created by zsc on 2018/5/14.
 */
public class ConsumerServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private Logger logger = LoggerFactory.getLogger(ConsumerServerHandler.class);
    private static ConcurrentHashMap<String,Channel> channelMap = new ConcurrentHashMap<>();
    private static RequestParser requestParser;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        requestParser = new RequestParser(fullHttpRequest);
        requestParser.parse();
        MessageRequest messageRequest = new MessageRequest(
                IdGenerator.getIdAndIncrement(),
                requestParser.getParmMap().get("interface"),
                requestParser.getParmMap().get("method"),
                requestParser.getParmMap().get("parameterTypesString"),
                requestParser.getParmMap().get("parameter")
        );
        AgentFuture<MessageResponse> future = sendRequest("com.alibaba.dubbo.performance.demo.provider.IHelloService",messageRequest,channelHandlerContext);
        Runnable callback = () -> {
            try {
                MessageResponse response = future.get();
                writeResponse(fullHttpRequest,fullHttpRequest,channelHandlerContext, (Integer) response.getResultDesc());
            } catch (Exception e) {
                FullHttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,HttpResponseStatus.BAD_REQUEST
                );
                channelHandlerContext.writeAndFlush(response);
                e.printStackTrace();
            }
        };
        WaitService.execute(callback);
    }

    private boolean writeResponse(HttpRequest request,HttpObject httpObject, ChannelHandlerContext ctx, int data) {
        logger.info("wwwwwwwwwwwriteResponse");
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                httpObject.decoderResult().isSuccess()? HttpResponseStatus.OK : HttpResponseStatus.BAD_REQUEST,
                Unpooled.copiedBuffer(String.valueOf(data).getBytes()));
        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        response.headers().add(CONTENT_TYPE,"application/json;charset=utf-8");
        response.headers().add(CONTENT_LENGTH,response.content().readableBytes());
        ctx.writeAndFlush(response,ctx.voidPromise());
        return keepAlive;
    }
    private AgentFuture<MessageResponse> sendRequest(String serviceName, MessageRequest request, ChannelHandlerContext channelHandlerContext) throws Exception {
        logger.info("sssssssssssendRequest");
        final Channel channel = channelHandlerContext.channel();
        AgentFuture<MessageResponse> future = new AgentFuture<>();
        Holder.putRequest(request.getMessageId(), future);
        Endpoint endpoint = LoadBalance.getEndpoint(serviceName);
        request.setEndpoint(endpoint);
        String key = channel.eventLoop().toString() + endpoint.toString();
        Channel nextChannel = channelMap.get(key);
        if (nextChannel == null) {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(channel.eventLoop())
                    .channel(EpollSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .handler(new ConsumerClientInitializer());
            ChannelFuture channelFuture = bootstrap.connect(endpoint.getHost(),endpoint.getPort());
            channelFuture.addListener(new ListenerImpl(request,endpoint));
        } else {
            nextChannel.writeAndFlush(request, nextChannel.voidPromise());
        }
        return future;
    }

    private static final class ListenerImpl implements ChannelFutureListener {
        private final Object objects;
        private final Endpoint endpoint;
        public ListenerImpl(Object object,Endpoint endpoint) {
            objects = object;
            this.endpoint = endpoint;
        }
        @Override
        public void operationComplete(ChannelFuture channelFuture) throws Exception {
            if (channelFuture.isSuccess()) {
                Channel channel = channelFuture.channel();
                channelMap.put(channel.eventLoop().toString() + endpoint.toString(),channel);
                channel.writeAndFlush(objects, channel.voidPromise());
            }
            else {
                channelFuture.channel().close();
            }
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("consumerServer异常", cause);
        cause.printStackTrace();
        ctx.close();
    }
}

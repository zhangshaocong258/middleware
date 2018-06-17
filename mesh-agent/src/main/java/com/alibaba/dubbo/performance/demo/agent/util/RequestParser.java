package com.alibaba.dubbo.performance.demo.agent.util;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zsc on 2018/6/15.
 */
public class RequestParser {
    private Logger logger = LoggerFactory.getLogger(RequestParser.class);
    private FullHttpRequest fullHttpRequest;
    Map<String, String> parmMap = new HashMap<>();

    /** * 构造一个解析器 * @param req */
    public RequestParser(FullHttpRequest req) {
        this.fullHttpRequest = req;
    }

    public Map<String, String> getParmMap() {
        return parmMap;
    }

    /** * 解析请求参数 * @return 包含所有请求参数的键值对, 如果没有参数, 则返回空Map * * @throws BaseCheckedException * @throws IOException */
    public void parse() throws IOException {
        HttpMethod method = fullHttpRequest.method();
        if (HttpMethod.GET == method) {
            // 是GET请求
            QueryStringDecoder decoder = new QueryStringDecoder(fullHttpRequest.uri());
            decoder.parameters().entrySet().forEach( entry -> {
                // entry.getValue()是一个List, 只取第一个元素
                parmMap.put(entry.getKey(), entry.getValue().get(0));
            });
        } else if (HttpMethod.POST == method) {
            // 是POST请求
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(fullHttpRequest);
            decoder.offer(fullHttpRequest);

            List<InterfaceHttpData> parmList = decoder.getBodyHttpDatas();

            for (InterfaceHttpData parm : parmList) {

                Attribute data = (Attribute) parm;
                parmMap.put(data.getName(), data.getValue());
            }

        } else {
            // 不支持其它方法
            logger.error("不支持其他方法"); // 这是个自定义的异常, 可删掉这一行
        }
    }
}
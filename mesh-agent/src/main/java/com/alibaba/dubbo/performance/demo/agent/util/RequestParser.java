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
 * Created by zsc on 2018/5/15.
 */
public class RequestParser {
    private Logger logger = LoggerFactory.getLogger(RequestParser.class);
    private FullHttpRequest fullHttpRequest;
    Map<String, String> parmMap = new HashMap<>();

    public RequestParser(FullHttpRequest req) {
        this.fullHttpRequest = req;
    }

    public Map<String, String> getParmMap() {
        return parmMap;
    }

    public void parse() throws IOException {
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(fullHttpRequest);
        decoder.offer(fullHttpRequest);

        List<InterfaceHttpData> parmList = decoder.getBodyHttpDatas();

        for (InterfaceHttpData parm : parmList) {

            Attribute data = (Attribute) parm;
            parmMap.put(data.getName(), data.getValue());
        }
    }
}
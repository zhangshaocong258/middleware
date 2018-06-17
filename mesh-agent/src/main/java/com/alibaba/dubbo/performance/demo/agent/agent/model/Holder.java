package com.alibaba.dubbo.performance.demo.agent.agent.model;
/**
 * Created by zsc on 2018/5/17.
 */

import java.util.concurrent.ConcurrentHashMap;

public class Holder {
    private static ConcurrentHashMap<String,AgentFuture<MessageResponse>> futureMap = new ConcurrentHashMap<>();
    public static AgentFuture<MessageResponse> removeRequest(String key){
        return futureMap.remove(key);
    }
    public static void putRequest(String key, AgentFuture<MessageResponse> future) {
        futureMap.put(key,future);
    }


}

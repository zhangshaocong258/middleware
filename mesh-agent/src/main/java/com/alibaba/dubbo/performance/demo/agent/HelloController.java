package com.alibaba.dubbo.performance.demo.agent;

import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClient;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@RestController
public class HelloController {

    private Logger logger = LoggerFactory.getLogger(HelloController.class);
    
    private IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));

    private RpcClient rpcClient = new RpcClient(registry);
    private List<Endpoint> endpoints = null;
    private Object lock = new Object();
    private OkHttpClient httpClient = new OkHttpClient.Builder().build();

    @RequestMapping(value = "")
    public Object invoke(@RequestParam("interface") String interfaceName,
                         @RequestParam("method") String method,
                         @RequestParam("parameterTypesString") String parameterTypesString,
                         @RequestParam("parameter") String parameter) throws Exception {
        String type = System.getProperty("type");   // 获取type参数
        if ("consumer".equals(type)){
            return consumer(interfaceName,method,parameterTypesString,parameter);
        }
        else if ("provider".equals(type)){
            return provider(interfaceName,method,parameterTypesString,parameter);
        }else {
            return "Environment variable type is needed to set to provider or consumer.";
        }
    }

    public byte[] provider(String interfaceName,String method,String parameterTypesString,String parameter) throws Exception {

        Object result = rpcClient.invoke(interfaceName,method,parameterTypesString,parameter);
        return (byte[]) result;
    }

    public Integer consumer(String interfaceName,String method,String parameterTypesString,String parameter) throws Exception {

//        if (null == endpoints){
//            synchronized (lock){
//                if (null == endpoints){
//                    endpoints = registry.find("com.alibaba.dubbo.performance.demo.provider.IHelloService");
//                }
//            }
//        }

        // 简单的负载均衡，随机取一个
        Endpoint endpoint = next();
        logger.info("endpoint " + endpoint.getHost());
        String url =  "http://" + endpoint.getHost() + ":" + endpoint.getPort();

        RequestBody requestBody = new FormBody.Builder()
                .add("interface",interfaceName)
                .add("method",method)
                .add("parameterTypesString",parameterTypesString)
                .add("parameter",parameter)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            byte[] bytes = response.body().bytes();
            String s = new String(bytes);
            return Integer.valueOf(s);
        }
    }

    public synchronized Endpoint next() throws Exception {
        if (null == endpoints) {
            synchronized (lock) {
                if (null == endpoints) {
                    endpoints = registry.find("com.alibaba.dubbo.performance.demo.provider.IHelloService");
                }
            }
            
        }
        int totalWeight = totalWeight(endpoints);
        for (Endpoint endpoint : endpoints) {
            endpoint.setCweight(endpoint.getWeight() + endpoint.getCweight());
        }
        Collections.sort(endpoints, COMPARATOR);
        Endpoint result = endpoints.get(0);
        result.setCweight(result.getCweight() - totalWeight);
        return result;
    }

    public int totalWeight(List<Endpoint> endpoints) {
        int totalWeight = 0;
        for (Endpoint endpoint : endpoints) {
            totalWeight += endpoint.getWeight();
        }
        return totalWeight;
    }

    private static final Comparator<Endpoint> COMPARATOR = new Comparator<Endpoint>() {
        public int compare(Endpoint o1, Endpoint o2) {
            if (o1.getCweight() < (o2.getCweight())) {
                return 1;
            } else {
                return -1;
            }
        }
    };
}

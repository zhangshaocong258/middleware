package com.alibaba.dubbo.performance.demo.agent.util;
/**
 * Created by zsc on 2018/5/6.
 */


import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;


public class LoadBalanceChoice {
    private static Logger logger = LoggerFactory.getLogger(LoggerFactory.class);
    private static final Random random = new Random();
    private static int pos = 0;
    private static EtcdRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));
    private static ConcurrentLinkedQueue<Endpoint> chooseQueue = new ConcurrentLinkedQueue();
    private static List<Endpoint> endpoints;
    private static final int TIMEWEIGHT = 12;
    private static final int LOCALWEIGHT = 12;
    private static AtomicInteger requestCount = new AtomicInteger(0);
    private static final int IGNORE_COUNT = 500;
    private static Object lock = new Object();
    private static Map<String,Integer> localWeight = new HashMap<>();
    private static List<Integer> timeWeight = new ArrayList<>();
    private static ConcurrentLinkedQueue queue = new ConcurrentLinkedQueue();
//    private static ConcurrentHashMap<String,Integer> executingTask = new ConcurrentHashMap<>();
    static {
//        executingTask.put("10.10.10.3",3);
//        executingTask.put("10.10.10.4",2);
//        executingTask.put("10.10.10.5",1);

        localWeight.put("10.10.10.3",1);
        localWeight.put("10.10.10.4",2);
        localWeight.put("10.10.10.5",3);
        timeWeight.add(2);
        timeWeight.add(3);
        timeWeight.add(4);

    }
    private LoadBalanceChoice() {
    }



    public static Endpoint weightedrandomChoice(String serviceName) throws Exception {
        checkEndpoint(serviceName);
        Endpoint endpoint = (Endpoint) queue.poll();
        if (endpoint == null) {
            ArrayList<Endpoint> arrayList = new ArrayList<>();
            for (Endpoint endpoint1 : endpoints) {
                int len = localWeight.get(endpoint1.getHost());
                for (int i=0;i<len;i++) {
                    arrayList.add(endpoint1);
                }
            }
            Collections.shuffle(arrayList);
            if (queue.isEmpty()) {
                queue = new ConcurrentLinkedQueue(arrayList);
            }
            return (Endpoint) queue.poll();
        }
        return endpoint;
    }

    private static void checkEndpoint(String serviceName) throws Exception {
        if (null == endpoints) {
            synchronized (lock) {
                if (null == endpoints) {
                    endpoints = registry.find(serviceName);
                }
            }
        }
    }

    public static Endpoint randomChoice(List<Endpoint> endpoints) {
        return endpoints.get(random.nextInt(endpoints.size()));
    }

    public static Endpoint roundChoice(List<Endpoint> endpoints) {
        return endpoints.get((pos++) % endpoints.size());
    }

}

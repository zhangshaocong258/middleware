package com.alibaba.dubbo.performance.demo.agent.util;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by zsc on 2018/5/17.
 */
public class LoadBalance {

    private static Logger logger = LoggerFactory.getLogger(LoadBalance.class);

    private static IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));

    private static List<Endpoint> endpoints = null;

    private static Object lock = new Object();

    public static synchronized Endpoint getEndpoint(String serviceName) throws Exception {
        if (null == endpoints) {
            synchronized (lock) {
                if (null == endpoints) {
                    endpoints = registry.find(serviceName);
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

    private static int totalWeight(List<Endpoint> endpoints) {
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

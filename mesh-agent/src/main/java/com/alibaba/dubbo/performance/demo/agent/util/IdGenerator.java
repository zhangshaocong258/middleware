package com.alibaba.dubbo.performance.demo.agent.util;

/**
 * Created by zsc on 2018/5/20.
 */

import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {

    private static AtomicInteger count = new AtomicInteger(0);

    public static String getIdAndIncrement() {
        return String.valueOf(count.getAndIncrement());
    }
}

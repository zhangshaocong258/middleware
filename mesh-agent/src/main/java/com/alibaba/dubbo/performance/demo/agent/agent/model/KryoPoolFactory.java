package com.alibaba.dubbo.performance.demo.agent.agent.model;

/**
 * Created by zsc on 2018/5/18.
 */

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import org.objenesis.strategy.StdInstantiatorStrategy;


public class KryoPoolFactory {
    private static KryoPoolFactory poolFactory = null;

    private KryoFactory factory = () ->{
            Kryo kryo = new Kryo();
            kryo.setReferences(false);
            kryo.register(AgentInvocation.class);
            kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
            return kryo;
    };
    private KryoPool pool = new KryoPool.Builder(factory).build();
    private KryoPoolFactory() {

    }

    public static KryoPool getKryoPoolInstance() {
        if (poolFactory == null) {
            synchronized (KryoPoolFactory.class) {
                if (poolFactory == null) {
                    poolFactory = new KryoPoolFactory();
                }
            }
        }
        return poolFactory.getPool();
    }

    public KryoPool getPool() {
        return pool;
    }
}

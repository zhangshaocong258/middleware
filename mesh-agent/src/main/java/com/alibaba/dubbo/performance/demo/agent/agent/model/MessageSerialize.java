package com.alibaba.dubbo.performance.demo.agent.agent.model;
/**
 * Created by zsc on 2018/5/18.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface MessageSerialize {
    void serialize(OutputStream outputStream, Object object) throws IOException;
    Object deserialize(InputStream inputStream) throws IOException;
}

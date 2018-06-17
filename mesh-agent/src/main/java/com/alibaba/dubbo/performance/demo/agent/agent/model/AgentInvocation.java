package com.alibaba.dubbo.performance.demo.agent.agent.model;

/**
 * Created by zsc on 2018/5/17.
 */

public class AgentInvocation {
    private String interfaceName;
    private String method;
    private String parameterTypesString;
    private String parameter;

    public AgentInvocation(String interfaceName, String method, String parameterTypesString, String parameter) {
        this.interfaceName = interfaceName;
        this.method = method;
        this.parameterTypesString = parameterTypesString;
        this.parameter = parameter;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public String getMethod() {
        return method;
    }

    public String getParameterTypesString() {
        return parameterTypesString;
    }

    public String getParameter() {
        return parameter;
    }
}

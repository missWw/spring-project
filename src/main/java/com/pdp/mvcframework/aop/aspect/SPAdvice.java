package com.pdp.mvcframework.aop.aspect;

import lombok.Data;

import java.lang.reflect.Method;

@Data
public class SPAdvice {
    
    //切面类
    private Object aspect;

    //切面方法
    private Method adviceMethod;

    //异常名称
    private String throwName;

    public SPAdvice(Object aspect, Method adviceMethod) {
        this.aspect = aspect;
        this.adviceMethod = adviceMethod;
    }
}

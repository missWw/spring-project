package com.pdp.mvcframework.aop;

import com.pdp.mvcframework.aop.aspect.SPAdvice;
import com.pdp.mvcframework.aop.support.SPAdvisedSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

public class SPJdkDynamicAopProxy implements InvocationHandler {

    private SPAdvisedSupport config;

    public SPJdkDynamicAopProxy(SPAdvisedSupport config) {
        this.config = config;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Map<String,SPAdvice> advices = config.getAdvices(method,null);
        
        Object returnValue;
        try {
            invokeAdivce(advices.get("before"));

            returnValue = method.invoke(this.config.getTarget(),args);

            invokeAdivce(advices.get("after"));
        }catch (Exception e){
            invokeAdivce(advices.get("afterThrow"));
            throw e;
        }

        return returnValue;
    }

    private void invokeAdivce(SPAdvice advice) {
        try {
            advice.getAdviceMethod().invoke(advice.getAspect());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public Object getProxy() {
        //用类加载器 实现this.config.getTargetClass().getInterfaces()所有接口 生成代理对象
        //h:动态代理方法在执行时，会调用h里面的invoke方法去执行
        return Proxy.newProxyInstance(this.getClass().getClassLoader(),this.config.getTargetClass().getInterfaces(),this);
    }
}

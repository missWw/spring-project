package com.pdp.mvcframework.beans;

public class SPBeanWrapper {

    //实例对象
    private Object wrapperInstance;
    //类对象
    private Class<?> wrapperClass;

    public SPBeanWrapper(Object instance) {

        this.wrapperInstance = instance;
        this.wrapperClass = instance.getClass();
    }

    public Object getWrapperInstance() {
        return wrapperInstance;
    }

    public void setWrapperInstance(Object wrapperInstance) {
        this.wrapperInstance = wrapperInstance;
    }

    public Class<?> getWrapperClass() {
        return wrapperClass;
    }

    public void setWrapperClass(Class<?> wrapperClass) {
        this.wrapperClass = wrapperClass;
    }
}

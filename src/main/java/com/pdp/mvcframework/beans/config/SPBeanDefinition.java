package com.pdp.mvcframework.beans.config;

public class SPBeanDefinition {

    //类名首字母大写或者自定义名称
    private String factoryBeanName;

    //包名加类名
    private String beanClassName;

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }


}

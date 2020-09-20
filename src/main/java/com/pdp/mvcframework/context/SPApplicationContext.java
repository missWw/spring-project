package com.pdp.mvcframework.context;

import com.pdp.mvcframework.annotation.SPAutowired;
import com.pdp.mvcframework.annotation.SPController;
import com.pdp.mvcframework.annotation.SPService;
import com.pdp.mvcframework.beans.SPBeanWrapper;
import com.pdp.mvcframework.beans.config.SPBeanDefinition;
import com.pdp.mvcframework.beans.support.SPBeanDefinitionReader;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SPApplicationContext {

    private SPBeanDefinitionReader reader;

    private Map<String, SPBeanDefinition> beanDefinitionMap = new HashMap<>();

    private Map<String, SPBeanWrapper> factoryBeanInstanceCache = new HashMap<>();
    private Map<String, Object> factoryBeanObjectCache = new HashMap<>();

    public SPApplicationContext(String... configLocations) {
        //1:加载配置文件

        reader = new SPBeanDefinitionReader(configLocations);

        try {
            //2:解析配置文件 封装成BeanDefinition  全类名和类名小写的BeanDefinition
            List<SPBeanDefinition> SPBeanDefinitions = reader.loadBeanDefinitions();

            //3:把BeanDefinition缓存起来
            doRegistBeanDefinition(SPBeanDefinitions);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //4:依赖注入
        doAutowired();
    }

    private void doAutowired() {

        for (Map.Entry<String, SPBeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            getBean(beanName);
        }

    }

    private void doRegistBeanDefinition(List<SPBeanDefinition> SPBeanDefinitions) throws Exception {

        for (SPBeanDefinition SPBeanDefinition : SPBeanDefinitions) {
            if (beanDefinitionMap.containsKey(SPBeanDefinition.getFactoryBeanName())) {
                throw new Exception("this" + SPBeanDefinition.getFactoryBeanName() + "is Exist");
            }
            beanDefinitionMap.put(SPBeanDefinition.getFactoryBeanName(), SPBeanDefinition);
            beanDefinitionMap.put(SPBeanDefinition.getBeanClassName(), SPBeanDefinition);
        }
    }

    /**
     * Bean的实例化，DI是从而这个方法开始的
     */
    public Object getBean(String beanName) {

        //1：获取配置信息
        SPBeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
        //2：反射实例化
        Object instance = instantinitBean(beanName, beanDefinition);

        System.out.println("beanName:"+beanName+"  对应的实例："+instance);

        //3：封装成BeanWrapper
        SPBeanWrapper beanWrapper = new SPBeanWrapper(instance);

        //4：保存到IoC容器
        factoryBeanInstanceCache.put(beanName, beanWrapper);

        //5：依赖注入
        populateBean(beanName, beanDefinition, beanWrapper);

        return beanWrapper.getWrapperInstance();
    }

    private void populateBean(String beanName, SPBeanDefinition beanDefinition, SPBeanWrapper beanWrapper) {

        //可能涉及到循环依赖？
        //A{ B b}
        //B{ A b}
        //用两个缓存，循环两次
        //1、把第一次读取结果为空的BeanDefinition存到第一个缓存
        //2、等第一次循环之后，第二次循环再检查第一次的缓存，再进行赋值

        Object instance = beanWrapper.getWrapperInstance();

        Class<?> clazz = instance.getClass();

        if(!(clazz.isAnnotationPresent(SPController.class) || clazz.isAnnotationPresent(SPService.class))) return;

        //遍历类的所有属性
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(SPAutowired.class)) return;

            SPAutowired autowired = field.getAnnotation(SPAutowired.class);

            String autowiredBeanName = autowired.value().trim();
            //如果autowired没有设置value 则使用属性名
            if ("".equals(autowiredBeanName)) {
                autowiredBeanName = field.getType().getName();
            }

            field.setAccessible(true);

            try {

                //解决循环依赖 如果没有就看BeanDefinition里有没有相关定义 有就获取相对应的实例
                if(!this.factoryBeanInstanceCache.containsKey(autowiredBeanName)){
                    if(this.beanDefinitionMap.containsKey(autowiredBeanName)){
                        Object autowiredInstance = this.getBean(autowiredBeanName);
                        field.set(instance,autowiredInstance);
                    }
                    continue;
                }

                field.set(instance, this.factoryBeanInstanceCache.get(autowiredBeanName).getWrapperInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }


    }

    private Object instantinitBean(String beanName, SPBeanDefinition beanDefinition) {
        String className = beanDefinition.getBeanClassName();
        Object instance = null;
        try {
            String  factoryBeanName = beanDefinition.getFactoryBeanName();
            String  beanClassName = beanDefinition.getBeanClassName();
            if(factoryBeanObjectCache.containsKey(beanClassName) || factoryBeanObjectCache.containsKey(factoryBeanName)){
                //如果同一个BeanDefinition  说明已经有了缓存
                instance = factoryBeanObjectCache.containsKey(beanClassName) ?  factoryBeanObjectCache.get(beanClassName) : factoryBeanObjectCache.get(factoryBeanName);
                this.factoryBeanObjectCache.put(beanClassName, instance);
            }else {
                Class clazz = Class.forName(className);
                instance = clazz.newInstance();
                this.factoryBeanObjectCache.put(beanClassName, instance);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        return instance;
    }


    public Object getBean(Class<?> beanClass) {
        return getBean(beanClass.getName());
    }

    public int getBeanDefinitionCount() {
        return this.beanDefinitionMap.size();
    }

    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }

    public Properties getConfig() {
        return this.reader.getConfig();
    }
}

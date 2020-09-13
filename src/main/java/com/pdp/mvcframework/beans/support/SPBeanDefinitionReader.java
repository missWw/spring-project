package com.pdp.mvcframework.beans.support;

import com.pdp.mvcframework.annotation.SPController;
import com.pdp.mvcframework.annotation.SPService;
import com.pdp.mvcframework.beans.config.SPBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SPBeanDefinitionReader {

    private List<String> registryBeanClasses = new ArrayList<>();

    private Properties properties = new Properties();


    public SPBeanDefinitionReader(String... configLocations) {
        doLoadConfig(configLocations[0]);

        //扫描配置文件中配置的相关的类
        doScanner(properties.getProperty("scanPackage"));
    }

    public List<SPBeanDefinition> loadBeanDefinitions() {
        List<SPBeanDefinition> result = new ArrayList<>();
        for (String className : registryBeanClasses) {

            try {
                Class<?> beanClass = Class.forName(className);

                //保存类对应的全类名
                //beanName
                //1：默认类名首字母小写
                //2：自定义的名称   先查看是不是有自定义的名称 有就用 没有就默认为首字母小写
                String beanName = beanClass.getAnnotation(SPController.class).value();
                //如果注解上有值 则以注解上面的值为准
                beanName = "".equals(beanName) ? toLowerFirstCase(beanClass.getSimpleName()) : beanName;
                result.add(doCreateBeanDefinition(toLowerFirstCase(beanName),beanClass.getName()));

                //3：接口注入
                for (Class<?> i : beanClass.getInterfaces()) {
                    result.add(doCreateBeanDefinition(i.getName(), beanClass.getName()));
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private SPBeanDefinition doCreateBeanDefinition(String beanName, String beanClassName) {
        SPBeanDefinition SPBeanDefinition = new SPBeanDefinition();
        SPBeanDefinition.setFactoryBeanName(beanName);
        SPBeanDefinition.setBeanClassName(beanClassName);
        return SPBeanDefinition;
    }

    private void doScanner(String scanPackage) {
        //通过传进来的需要扫描的类的目录scanPackage 进行扫描
        URL url = this.getClass().getResource("/" + scanPackage.replaceAll("\\.", "/"));

        File classPath = new File(url.getFile());

        for (File file : classPath.listFiles()) {
            if (file.isDirectory()) {
                //如果是目录  则递归对该目录进行扫描
                doScanner(scanPackage + "." + file.getName());
            } else {

                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                String className = scanPackage + "." + file.getName().replaceAll(".class", "");
                registryBeanClasses.add(className);
            }
        }


    }

    private String toLowerFirstCase(String simpleName) {
        if (simpleName == null) return null;
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    private void doLoadConfig(String config) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(config.replaceAll("classpath:", ""));
        try {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}



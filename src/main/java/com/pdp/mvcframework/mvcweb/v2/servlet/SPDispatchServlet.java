package com.pdp.mvcframework.mvcweb.v2.servlet;


import com.pdp.mvcframework.annotation.*;
import com.pdp.mvcframework.context.SPApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class SPDispatchServlet extends HttpServlet {

    private SPApplicationContext applicationContext;

    private List<String> classNames = new ArrayList<String>();

    private Map<String, Object> ioc = new HashMap<String, Object>();

    private Map<String, Method> handlerMapping = new HashMap<String, Method>();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        //6:委派 根据url找到对应的方法并通过response返回
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        //获取url
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        //去掉contextPath  因为 handleMapping 存的不是全路径
        url = url.replaceAll(contextPath, "").replaceAll("/+", "/");
        System.out.println("url:" + url);
        //路径中没有在handlemapping中找到
        if (!handlerMapping.containsKey(url)) {
            resp.getWriter().write("404 Not Found!!!");
            return;
        }

        Map<String, String[]> param = req.getParameterMap();

        Method method = handlerMapping.get(url);


        //获取参数列表 并赋值  关键的一步
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] paramValues = new Object[parameterTypes.length];

        //遍历参数
        for (int i = 0; i < parameterTypes.length; i++) {
            Class parameterType = parameterTypes[i];
            if (parameterType == HttpServletRequest.class) {
                paramValues[i] = req;
            } else if (parameterType == HttpServletResponse.class) {
                paramValues[i] = resp;
            } else if (parameterType == String.class) {
                //必须通过运行时的状态去拿到你
                Annotation[][] pa = method.getParameterAnnotations();
                for (int j = 0; j < pa.length; j++) {
                    for (Annotation an : pa[j]) {
                        if (an instanceof SPRequestParam) {
                            String paramName = ((SPRequestParam) an).value();
                            if (!"".equals(paramName.trim())) {
                                String value = Arrays.toString(param.get(paramName))
                                        .replaceAll("\\[|\\]", "")
                                        .replaceAll("\\s+", ",");
                                paramValues[i] = value;
                            }
                        }
                    }
                }


            }


        }


        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
        System.out.println("方法执行前：" + method);
        //赋值实参列表
        method.invoke(ioc.get(beanName), paramValues);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {

        applicationContext = new SPApplicationContext(config.getInitParameter("contextConfigLocation"));

        /*//1:加载配置文件
        doLoadConfig(config);

        //2:扫描相关的类
        doScanner(properties.getProperty("scanPackage"));

        //3：初始化扫描的类 加载到IOC容器
        doInstance();

        //4:完成依赖注入
        doAutowired();*/

        //5:初始化 HandleMapping
        initHandleMapping();

        System.out.println("MVC Framework is init");
    }

    private void initHandleMapping() {

        if (this.applicationContext.getBeanDefinitionCount() <= 0) return;
        String[] names = this.applicationContext.getBeanDefinitionNames();
        for (String beanName : names) {
            //获取IoC容器的对象
            Class clazz = this.applicationContext.getBean(beanName).getClass();

            //如果类上面没有Controller的注解 则返回
            if (!clazz.isAnnotationPresent(SPController.class)) return;

            //提取类上面mapping的值
            String baseUrl = "";
            if (clazz.isAnnotationPresent(SPRequestMapping.class)) {
                SPRequestMapping requestMapping = (SPRequestMapping) clazz.getAnnotation(SPRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            //只获取public的方法

            Method[] methods = clazz.getMethods();

            for (Method method : methods) {
                if (method.isAnnotationPresent(SPRequestMapping.class)) {
                    SPRequestMapping requestMapping = (SPRequestMapping) method.getAnnotation(SPRequestMapping.class);
                    String url = ("/" + baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");
                    System.out.println("url:" + url);
                    handlerMapping.put(url, method);
                    System.out.println("Mapped:" + url + "," + method);
                }
            }

        }


    }

    private String toLowerFirstCase(String simpleName) {
        if (simpleName == null) return null;
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

}

package com.pdp.mvcframework.v1.servlet;


import com.pdp.mvcframework.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class DispatchServlet extends HttpServlet {

    private Properties properties = new Properties();

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
                        if (an instanceof WRequestParam) {
                            String paramName = ((WRequestParam) an).value();
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

        //1:加载配置文件
        doLoadConfig(config);

        //2:扫描相关的类
        doScanner(properties.getProperty("scanPackage"));

        //3：初始化扫描的类 加载到IOC容器
        doInstance();

        //4:完成依赖注入
        doAutowired();

        //5:初始化 HandleMapping
        initHandleMapping();

        System.out.println("MVC Framework is init");
    }


    private void doLoadConfig(ServletConfig config) {
        //通过ServletConfig获取Servlet的名称找到配置文件的名称 然后在项目的目录下找到这个文件 当做流传给Properties
        String contextConfigLocation = config.getInitParameter("contextConfigLocation");
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
                classNames.add(className);
            }
        }


    }

    private void doInstance() {
        if (classNames.isEmpty()) {
            return;
        }
        try {
            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);
                //isAnnotationPresent 类上是否有该参数类型的注解
                if (clazz.isAnnotationPresent(WController.class)) {
                    //有就把value取出来
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    System.out.println("WController:" + beanName);
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);
                } else if (clazz.isAnnotationPresent(WService.class)) {

                    //1、在多个包下出现相同的类名，只能寄几（自己）起一个全局唯一的名字
                    //有自定义命名用自定义的 没有用类名首字母小写
                    String beanName = clazz.getAnnotation(WService.class).value();
                    System.out.println("WService:" + beanName);
                    //如果注解上有值 则以注解上面的值为准
                    beanName = "".equals(beanName) ? toLowerFirstCase(clazz.getSimpleName()) : beanName;

                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);

                    //3、如果是接口
                    //判断有多少个实现类，如果只有一个，默认就选择这个实现类
                    //如果有多个，只能抛异常
                    //如果class有实现接口 那就把接口的包名+类名作为bean名称加入到ioc容器里面
                    for (Class<?> i : clazz.getInterfaces()) {
                        if (ioc.containsKey((i.getName()))) {
                            throw new Exception("The " + i.getName() + " is exists!!");
                        }
                        ioc.put(i.getName(), instance);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void doAutowired() {
        //如果ioc容器为空 则表示没有初始化
        if (ioc.isEmpty()) return;

        //初始化IoC容器里面的类的属性注入
        //把所有的包括private/protected/default/public 修饰字段都取出来
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {

            //遍历类的所有属性
            for (Field field : entry.getValue().getClass().getDeclaredFields()) {
                if (!field.isAnnotationPresent(WAutowired.class)) return;

                WAutowired wAutowired = field.getAnnotation(WAutowired.class);

                String beanName = wAutowired.value().trim();
                //如果autowired没有设置value 则使用属性名
                if ("".equals(beanName)) {
                    beanName = field.getType().getName();
                }

                field.setAccessible(true);

                try {
                    //设置属性值为 ioc容器的实例
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    private void initHandleMapping() {

        if (ioc.isEmpty()) return;

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            //获取IoC容器的对象
            Class clazz = entry.getValue().getClass();

            //如果类上面没有Controller的注解 则返回
            if (!clazz.isAnnotationPresent(WController.class)) return;

            //提取类上面mapping的值
            String baseUrl = "";
            if (clazz.isAnnotationPresent(WRequestMapping.class)) {
                WRequestMapping requestMapping = (WRequestMapping) clazz.getAnnotation(WRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            //只获取public的方法

            Method[] methods = clazz.getMethods();

            for (Method method : methods) {
                if (method.isAnnotationPresent(WRequestMapping.class)) {
                    WRequestMapping requestMapping = (WRequestMapping) method.getAnnotation(WRequestMapping.class);
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

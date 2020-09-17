package com.pdp.mvcframework.mvcweb.v2.servlet;


import com.pdp.mvcframework.annotation.*;
import com.pdp.mvcframework.context.SPApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SPDispatchServlet extends HttpServlet {

    private List<SPHandlerMapping> handlerMappings = new ArrayList<>();

    private SPApplicationContext applicationContext;

    private Map<SPHandlerMapping, SPHandlerAdapter> handlerAdapters = new HashMap<>();

    private List<SPViewResolver> viewResolvers = new ArrayList<SPViewResolver>();


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
            try {
                processDispatchResult(req, resp, new SPModelAndView("500"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        //1：通过一个url获得一个 HandlerMapping
        SPHandlerMapping handler = this.getHandler(req);

        if (handler == null) {
            processDispatchResult(req, resp, new SPModelAndView("404"));
            return;
        }

        //2：通过一个 HandlerMapping 获取一个 HandlerAdapter

        SPHandlerAdapter adapter = getHandlerAdapter(handler);

        //3：解析某一个方法的形参和返回值之后 统一封装成 ModelAndView对象
        SPModelAndView modelAndView = adapter.handler(req, resp, handler);

        //4： 把ModelAndView对象 变为ViewResolver
        processDispatchResult(req, resp, modelAndView);


    }

    /**
     * 获取 HandlerMapping 对应的 HandlerAdapter
     *
     * @param handler
     * @return
     */
    private SPHandlerAdapter getHandlerAdapter(SPHandlerMapping handler) {
        if (handlerAdapters.isEmpty()) return null;
        return handlerAdapters.get(handler);
    }


    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, SPModelAndView modelAndView) throws Exception {
        if (modelAndView == null) return;

        if (this.viewResolvers.isEmpty()) return;

        for (SPViewResolver viewResolver : this.viewResolvers) {
            SPView view = viewResolver.resolverViewName(modelAndView.getViewName());
            //直接往浏览器输出
            view.render(modelAndView.getModel(),req,resp);
            return;
        }
    }


    private SPHandlerMapping getHandler(HttpServletRequest req) {
        if (handlerMappings.isEmpty()) return null;

        //获取url
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        //去掉contextPath  因为 handleMapping 存的不是全路径
        url = url.replaceAll(contextPath, "").replaceAll("/+", "/");
        System.out.println("url:" + url);

        for (SPHandlerMapping handlerMapping : handlerMappings) {
            Matcher matcher = handlerMapping.getPattern().matcher(url);
            if (matcher.matches()) {
                return handlerMapping;
            }
        }

        return null;
    }


    @Override
    public void init(ServletConfig config) throws ServletException {

        //初始化IoC容器
        applicationContext = new SPApplicationContext(config.getInitParameter("contextConfigLocation"));


        //完成了IoC、DI和MVC部分对接

        //初始化MVC组件  九大组件
        initStrategies(applicationContext);


        System.out.println("MVC Framework is init");
    }

    private void initStrategies(SPApplicationContext context) {
//        //多文件上传的组件
//        initMultipartResolver(context);
//        //初始化本地语言环境
//        initLocaleResolver(context);
//        //初始化模板处理器
//        initThemeResolver(context);

        //初始化handlermapping
        initHandleMapping(context);

        initHandlerAdapters(context);

        //初始化异常拦截器
//        initHandlerExceptionResolvers(context);
//        //初始化视图预处理器
//        initRequestToViewNameTranslator(context);

        //初始化视图转换器
        initViewResolvers(context);
//        //FlashMap管理器
//        initFlashMapManager(context);
    }

    private void initViewResolvers(SPApplicationContext context) {
        String templateRoot = context.getConfig().getProperty("templateRoot");

        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir = new File(templateRootPath);
        for (File file : templateRootDir.listFiles()) {
            this.viewResolvers.add(new SPViewResolver(templateRoot));
        }

    }

    private void initHandlerAdapters(SPApplicationContext context) {
        for (SPHandlerMapping handlerMapping : handlerMappings) {
            handlerAdapters.put(handlerMapping, new SPHandlerAdapter());
        }
    }

    private void initHandleMapping(SPApplicationContext context) {

        if (this.applicationContext.getBeanDefinitionCount() <= 0) return;
        String[] names = this.applicationContext.getBeanDefinitionNames();
        for (String beanName : names) {
            //获取IoC容器的对象
            Object instance = this.applicationContext.getBean(beanName);
            Class clazz = instance.getClass();

            //如果类上面没有Controller的注解 则返回
            if (!clazz.isAnnotationPresent(SPController.class)) continue;

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
                    String url = ("/" + baseUrl + "/" + requestMapping.value()).replaceAll("\\*", ".*").replaceAll("/+", "/");
                    Pattern pattern = Pattern.compile(url);
                    System.out.println("pattern:" + pattern);
                    handlerMappings.add(new SPHandlerMapping(pattern, method, instance));
                    System.out.println("Mapped:" + url + "," + method);
                }
            }

        }


    }


}

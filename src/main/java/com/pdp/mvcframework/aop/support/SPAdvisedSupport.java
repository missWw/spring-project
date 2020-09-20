package com.pdp.mvcframework.aop.support;

import com.pdp.mvcframework.aop.aspect.SPAdvice;
import com.pdp.mvcframework.aop.config.SPAopConfig;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SPAdvisedSupport {

    private SPAopConfig config;

    //目标类实例对象
    private Object target;

    //目标类 类对象
    private Class targetClass;

    private Pattern pointCutClassPattern;

    private Map<Method, Map<String, SPAdvice>> methodCache;

    public SPAdvisedSupport(SPAopConfig config) {
        this.config = config;
    }

    private void parse() {

        //把Spring的Excpress变成Java能够识别的正则表达式
        String pointCut = this.config.getPointCut()
                .replaceAll("\\.","\\\\.")
                .replaceAll("\\\\.\\*",".*")
                .replaceAll("\\(","\\\\(")
                .replaceAll("\\)","\\\\)");

        //保存专门匹配Class的正则
        String pointCutForClassRegex = pointCut.substring(0, pointCut.lastIndexOf("\\(") - 4);
        pointCutClassPattern = Pattern.compile("class " + pointCutForClassRegex.substring(pointCutForClassRegex.lastIndexOf(" ") + 1));


        //享元的共享池
        methodCache =new HashMap<>();
        //保存专门匹配方法的正则
        Pattern pointCutPattern = Pattern.compile(pointCut);

        try {
            //切面类
            Class aspectClass = Class.forName(this.config.getAspectClass());
            //切面方法
            Map<String,Method> aspectMethods = new HashMap<>();
            for (Method m : aspectClass.getMethods()){
                aspectMethods.put(m.getName(),m);
            }

            for (Method method : this.targetClass.getMethods()){
                String methodString  = method.toString();
                //如果方法后面抛出了异常 去掉方法抛出的异常
                if(methodString.contains("throws")){
                    methodString = methodString.substring(0,methodString.lastIndexOf("throws")).trim();
                }

                Matcher matcher = pointCutPattern.matcher(methodString);
                //如果方法全名符合切面的正则
                if(matcher.matches()){
                    Map<String,SPAdvice> advices = new HashMap<>();

                    if(!(null == config.getAspectBefore() || "".equals(config.getAspectBefore()))){
                        advices.put("before",new SPAdvice(aspectClass.newInstance(),aspectMethods.get(config.getAspectBefore())));
                    }
                    if(!(null == config.getAspectAfter() || "".equals(config.getAspectAfter()))){
                        advices.put("after",new SPAdvice(aspectClass.newInstance(),aspectMethods.get(config.getAspectAfter())));
                    }
                    if(!(null == config.getAspectAfterThrow() || "".equals(config.getAspectAfterThrow()))){
                        SPAdvice advice = new SPAdvice(aspectClass.newInstance(),aspectMethods.get(config.getAspectAfterThrow()));
                        advice.setThrowName(config.getAspectAfterThrowingName());
                        advices.put("afterThrow",advice);
                    }

                    //跟目标代理类的业务方法和Advices建立一对多个关联关系，以便在Porxy类中获得
                    methodCache.put(method,advices);
                    System.out.println(methodCache);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //根据一个目标代理类的方法，获得其对应的通知
    public Map<String,SPAdvice> getAdvices(Method method, Object o) throws Exception {
        Map<String,SPAdvice> cache = methodCache.get(method);

        if(null == cache){
            Method m = targetClass.getMethod(method.getName(),method.getParameterTypes());
            cache = methodCache.get(m);
            this.methodCache.put(m,cache);
        }
        return cache;
    }

    public boolean pointCutMath() {
        //用正则表达式判断当前的类的全类名是否符合 配置文件pointcut规则定义的类
        // 如果是 就说明需要生成aop代理对象
        return pointCutClassPattern.matcher(this.targetClass.toString()).matches();
    }


    public Class getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
        parse();
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    
}

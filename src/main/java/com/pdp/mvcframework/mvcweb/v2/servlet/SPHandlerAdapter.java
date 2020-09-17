package com.pdp.mvcframework.mvcweb.v2.servlet;

import com.pdp.mvcframework.annotation.SPRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class SPHandlerAdapter {


    public SPModelAndView handler(HttpServletRequest req, HttpServletResponse resp, SPHandlerMapping handler) throws Exception {

        //保存形参列表
        //将参数的名称和位置保存起来
        Map<String,Integer> paramIndexMapping = new HashMap<>();


        //必须通过运行时的状态去拿到你
        Annotation[][] pa = handler.getMethod().getParameterAnnotations();
        for (int j = 0; j < pa.length; j++) {
            for (Annotation an : pa[j]) {
                if (an instanceof SPRequestParam) {
                    String paramName = ((SPRequestParam) an).value();
                    if (!"".equals(paramName.trim())) {
                        /*String value = Arrays.toString(param.get(paramName))
                                .replaceAll("\\[|\\]", "")
                                .replaceAll("\\s+", ",");*/
                        paramIndexMapping.put(paramName,j);
                    }
                }
            }
        }

        Class<?>[] paramTypes = handler.getMethod().getParameterTypes();

        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramterType = paramTypes[i];
            if(paramterType == HttpServletRequest.class || paramterType == HttpServletResponse.class){
                paramIndexMapping.put(paramterType.getName(),i);
            }
        }

        //去拼接实参列表
        //http://localhost/web/query?name=Tom&Cat
        Map<String, String[]> params = req.getParameterMap();

        //实参列表
        Object[] paramValues = new Object[paramTypes.length];

        for(Map.Entry<String,String[]> param:params.entrySet()){

            String value = Arrays.toString(params.get(param.getKey()))
                    .replaceAll("\\[|\\]","")
                    .replaceAll("\\s+",",");

            if (!paramIndexMapping.containsKey(param.getKey())) continue;

            //获取参数在形参列表中的位置
            int index = paramIndexMapping.get(param.getKey());

            //允许自定义的类型转换器Converter
            paramValues[index] = castStringValue(value,paramTypes[index]);

        }
        //如果参数
        if (paramIndexMapping.containsKey(HttpServletRequest.class.getName())){
            int index = paramIndexMapping.get(HttpServletRequest.class.getName());
            paramValues[index] = req;
        }

        if (paramIndexMapping.containsKey(HttpServletResponse.class.getName())){
            int index = paramIndexMapping.get(HttpServletResponse.class.getName());
            paramValues[index] = resp;
        }

        //返回一个ModelAndView
        Object result = handler.getMethod().invoke(handler.getController(),paramValues);
        if (result == null || result instanceof Void) return null;

        boolean isModelAndView = handler.getMethod().getReturnType() == SPModelAndView.class;

        if(isModelAndView){
            return (SPModelAndView)result;
        }
        return null;

    }

    private Object castStringValue(String value, Class<?> paramType) {
        if(String.class == paramType){
            return value;
        }else if(Integer.class == paramType){
            return Integer.valueOf(value);
        }else if(Double.class == paramType){
            return Double.valueOf(value);
        }else {
            if(value != null){
                return value;
            }
            return null;
        }
    }

}

package com.pdp.demo.controller;

import com.pdp.demo.service.IDemoService;
import com.pdp.mvcframework.annotation.SPAutowired;
import com.pdp.mvcframework.annotation.SPController;
import com.pdp.mvcframework.annotation.SPRequestMapping;
import com.pdp.mvcframework.annotation.SPRequestParam;
import com.pdp.mvcframework.mvcweb.v2.servlet.SPModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


//虽然，用法一样，但是没有功能
@SPController
@SPRequestMapping("/web")
public class DemoController {

    @SPAutowired
    private IDemoService demoService;

    

    @SPRequestMapping("/add*.json")
    public SPModelAndView add(HttpServletRequest request, HttpServletResponse response,
                              @SPRequestParam("name") String name) {
        String result = null;
        try {
            result = demoService.add(name);
        } catch (Exception e) {

            Map<String,String> model = new HashMap<String,String>();
            model.put("detail",e.getCause().getMessage());
            model.put("stackTrace", Arrays.toString(e.getStackTrace()));
            return new SPModelAndView("500",model);
        }
        return out(response, result);
    }

    private SPModelAndView out(HttpServletResponse resp, String str) {
        try {
            resp.getWriter().write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}

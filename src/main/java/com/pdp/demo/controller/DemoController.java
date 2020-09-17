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


//虽然，用法一样，但是没有功能
@SPController
@SPRequestMapping("/web")
public class DemoController {

    @SPAutowired
    private IDemoService demoService;

    @SPRequestMapping("/query")
    public void query(HttpServletRequest req, HttpServletResponse resp,
                      @SPRequestParam("name") String name) {
        String result = demoService.get(name);
//		String result = "My name is " + name;
        try {
            resp.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SPRequestMapping("/add1")
    public void add(HttpServletRequest req, HttpServletResponse resp,
                    @SPRequestParam("a") Integer a, @SPRequestParam("b") Integer b) {
        try {
            resp.getWriter().write(a + "+" + b + "=" + (a + b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SPRequestMapping("/sub")
    public void add(HttpServletRequest req, HttpServletResponse resp,
                    @SPRequestParam("a") Double a, @SPRequestParam("b") Double b) {
        try {
            resp.getWriter().write(a + "-" + b + "=" + (a - b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SPRequestMapping("/remove")
    public String remove(@SPRequestParam("id") Integer id) {
        return "" + id;
    }

    @SPRequestMapping("/add*.json")
    public SPModelAndView add(HttpServletRequest request, HttpServletResponse response,
                              @SPRequestParam("name") String name) {
        String result = name + "name ";
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

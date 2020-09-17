package com.pdp.demo.controller;

import com.pdp.demo.service.QueryService;
import com.pdp.mvcframework.annotation.SPAutowired;
import com.pdp.mvcframework.annotation.SPController;
import com.pdp.mvcframework.annotation.SPRequestMapping;
import com.pdp.mvcframework.annotation.SPRequestParam;
import com.pdp.mvcframework.mvcweb.v2.servlet.SPModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * 公布接口url
 * @author Tom
 *
 */
@SPController
@SPRequestMapping("/")
public class PageController {

    @SPAutowired
    QueryService queryService;

    @SPRequestMapping("/first.html")
    public SPModelAndView query(@SPRequestParam("teacher") String teacher){
        String result = queryService.query(teacher);
        Map<String,Object> model = new HashMap<String,Object>();
        model.put("teacher", teacher);
        model.put("data", result);
        model.put("token", "123456");
        return new SPModelAndView("first.html",model);
    }

}

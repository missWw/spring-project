package com.pdp.demo;

import com.pdp.demo.service.IDemoService;
import com.pdp.mvcframework.annotation.SPAutowired;
import com.pdp.mvcframework.annotation.SPController;
import com.pdp.mvcframework.annotation.SPRequestMapping;
import com.pdp.mvcframework.annotation.SPRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


//虽然，用法一样，但是没有功能
@SPController
@SPRequestMapping("/demo")
public class DemoAction {

  	@SPAutowired
	private IDemoService demoService;

	@SPRequestMapping("/query")
	public void query(HttpServletRequest req, HttpServletResponse resp,
					  @SPRequestParam("name") String name){
		String result = demoService.get(name);
//		String result = "My name is " + name;
		try {
			resp.getWriter().write(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SPRequestMapping("/add")
	public void add(HttpServletRequest req, HttpServletResponse resp,
					@SPRequestParam("a") Integer a, @SPRequestParam("b") Integer b){
		try {
			resp.getWriter().write(a + "+" + b + "=" + (a + b));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SPRequestMapping("/sub")
	public void add(HttpServletRequest req, HttpServletResponse resp,
					@SPRequestParam("a") Double a, @SPRequestParam("b") Double b){
		try {
			resp.getWriter().write(a + "-" + b + "=" + (a - b));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SPRequestMapping("/remove")
	public String remove(@SPRequestParam("id") Integer id){
		return "" + id;
	}

}

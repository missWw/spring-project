package com.pdp.demo;

import com.pdp.demo.service.IDemoService;
import com.pdp.mvcframework.annotation.WAutowired;
import com.pdp.mvcframework.annotation.WController;
import com.pdp.mvcframework.annotation.WRequestMapping;
import com.pdp.mvcframework.annotation.WRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


//虽然，用法一样，但是没有功能
@WController
@WRequestMapping("/demo")
public class DemoAction {

  	@WAutowired
	private IDemoService demoService;

	@WRequestMapping("/query")
	public void query(HttpServletRequest req, HttpServletResponse resp,
					  @WRequestParam("name") String name){
		String result = demoService.get(name);
//		String result = "My name is " + name;
		try {
			resp.getWriter().write(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@WRequestMapping("/add")
	public void add(HttpServletRequest req, HttpServletResponse resp,
                    @WRequestParam("a") Integer a, @WRequestParam("b") Integer b){
		try {
			resp.getWriter().write(a + "+" + b + "=" + (a + b));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@WRequestMapping("/sub")
	public void add(HttpServletRequest req, HttpServletResponse resp,
                    @WRequestParam("a") Double a, @WRequestParam("b") Double b){
		try {
			resp.getWriter().write(a + "-" + b + "=" + (a - b));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@WRequestMapping("/remove")
	public String remove(@WRequestParam("id") Integer id){
		return "" + id;
	}

}

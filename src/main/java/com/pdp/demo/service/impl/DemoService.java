package com.pdp.demo.service.impl;

import com.pdp.demo.service.IDemoService;
import com.pdp.mvcframework.annotation.SPService;

/**
 * 核心业务逻辑
 */
@SPService
public class DemoService implements IDemoService {

	public String get(String name) {
		return "My name is " + name + ",from service.";
	}

	public String add(String name) throws Exception{
		throw new Exception("这是故意抛出来的异常");
	}


}

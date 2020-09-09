package com.pdp.demo.service.impl;

import com.pdp.demo.service.IDemoService;
import com.pdp.mvcframework.annotation.WService;

/**
 * 核心业务逻辑
 */
@WService
public class DemoService implements IDemoService {

	public String get(String name) {
		return "My name is " + name + ",from service.";
	}

}

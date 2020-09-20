package com.pdp.aspect;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogAspect {

    public void before(){
        log.info("Invoker Before Method!!!");
    }

    public void after(){
        log.info("Invoker After Method!!!");
    }

    public void afterThrowing(){
        log.info("出现异常");
    }
}

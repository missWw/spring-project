package com.pdp.mvcframework.aop.config;

import lombok.Data;

@Data
public class SPAopConfig {

    private String pointCut;
    private String aspectClass;
    private String aspectBefore;
    private String aspectAfter;
    private String aspectAfterThrow;
    private String aspectAfterThrowingName;


}

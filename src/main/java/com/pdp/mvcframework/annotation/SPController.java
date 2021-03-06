package com.pdp.mvcframework.annotation;


import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SPController {

    String value() default "";
}

package com.pdp.mvcframework.mvcweb.v2.servlet;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class SPHandlerMapping {

    private Pattern pattern;
    private Method method;
    private Object controller;

    public SPHandlerMapping(Pattern pattern, Method method, Object controller) {
        this.pattern = pattern;
        this.method = method;
        this.controller = controller;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }
}

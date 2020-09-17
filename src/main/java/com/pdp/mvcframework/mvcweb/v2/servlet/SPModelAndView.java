package com.pdp.mvcframework.mvcweb.v2.servlet;

import java.util.Map;

public class SPModelAndView {
    private String viewName;
    private Map<String,?> model;

    public SPModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public SPModelAndView(String viewName) {
        this.viewName = viewName;
    }

    public Map<String, ?> getModel() {
        return model;
    }

    public String getViewName() {
        return viewName;
    }
}

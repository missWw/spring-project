package com.pdp.mvcframework.mvcweb.v2.servlet;

import java.io.File;

public class SPViewResolver {

    private final String DEFAULT_TEMPLATE_SUFFIX = ".html";

    private File templateRootDir;

    public SPViewResolver(String templateRootDir) {
        //获取指定模板名称下的文件全路径
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRootDir).getFile();

        this.templateRootDir = new File(templateRootPath);
    }

    public SPView resolverViewName(String viewName) {

        if (viewName == null || "".equals(viewName.trim())) return null;

        viewName = viewName.endsWith(DEFAULT_TEMPLATE_SUFFIX) ? viewName : (viewName + DEFAULT_TEMPLATE_SUFFIX);

        File templateFile = new File((templateRootDir.getPath() + "/" + viewName).replaceAll("/+", "/"));
        return new SPView(templateFile);

    }
}

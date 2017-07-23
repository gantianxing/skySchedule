package com.sky.schedule.server.spring;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * Created by gantianxing on 2017/7/20.
 */
public class SkyScheduleInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    /**
     * 指定ContextLoaderListener配置类，相对于spring-config.xml
     * @return
     */
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[]{RootConfig.class};
    }

    /**
     * 指定spring mvc配置类 相当于spring-mvc.xml
     * @return
     */
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[]{WebConfig.class};
    }

    /**
     * 将 DispatcherServlet映射到 "/"
     * @return
     */
    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }
}

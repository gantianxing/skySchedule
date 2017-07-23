package com.sky.schedule.server.spring;

import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Created by gantianxing on 2017/7/20.
 */

@Configuration
@ComponentScan(basePackages = {"com.sky.schedule"},
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ANNOTATION, value = EnableWebMvc.class)})
@PropertySource("classpath:SkySchedule-server.properties")
public class RootConfig {

    @Bean
    public PropertySourcesPlaceholderConfigurer placeholderConfigurer(){
        return new PropertySourcesPlaceholderConfigurer();
    }
}

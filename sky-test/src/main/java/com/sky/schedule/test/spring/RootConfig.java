package com.sky.schedule.test.spring;

import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Created by gantianxing on 2017/7/20.
 */

@Configuration
@ComponentScan(basePackages = {"com.sky.schedule.client"})
@PropertySource("classpath:SkySchedule-client.properties")
public class RootConfig {

    @Bean
    public PropertySourcesPlaceholderConfigurer placeholderConfigurer(){
        return new PropertySourcesPlaceholderConfigurer();
    }
}

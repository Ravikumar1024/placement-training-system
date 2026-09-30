package com.placement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebAppConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/webapp/**")
                .addResourceLocations("file:./webapp/");
        registry.addResourceHandler("/css/**")
                .addResourceLocations("file:./webapp/css/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("file:./webapp/js/");
    }
}

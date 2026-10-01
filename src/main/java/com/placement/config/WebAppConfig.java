package com.placement.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebAppConfig implements WebMvcConfigurer {
        private final MessageSource messageSource;

        public WebAppConfig(MessageSource messageSource) {
                this.messageSource = messageSource;
        }

        @Bean
        LocalValidatorFactoryBean validator() {
                LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
                validator.setValidationMessageSource(messageSource);
                return validator;
        }

        @Override
        public org.springframework.validation.Validator getValidator() {
                return validator();
        }

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

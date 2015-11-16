/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.beanconfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.find.hod.converter.StringToResourceIdentifierConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.joda.JodaDateTimeFormatAnnotationFormatterFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import java.util.List;
import java.util.Properties;

@Configuration
@ComponentScan(
    basePackages = {
        "com.hp.autonomy.frontend.find"
    },
    includeFilters = {
        @ComponentScan.Filter(Controller.class),
        @ComponentScan.Filter(RestController.class),
    },
    excludeFilters = {
        @ComponentScan.Filter(Configuration.class)
    }
)
public class DispatcherServletConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private ObjectMapper dispatcherObjectMapper;

    @Autowired
    private Properties dispatcherProperties;

    @Bean
    public ViewResolver internalResourceViewResolver() {
        final InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setViewClass(JstlView.class);
        viewResolver.setPrefix("/WEB-INF/jsps/");
        viewResolver.setSuffix(".jsp");

        return viewResolver;
    }

    @Override
    public void extendMessageConverters(final List<HttpMessageConverter<?>> converters) {
        final MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(dispatcherObjectMapper);

        converters.add(converter);
    }

    @Override
    public void addFormatters(final FormatterRegistry registry) {
        registry.addConverter(new StringToResourceIdentifierConverter());

        registry.addFormatterForFieldAnnotation(new JodaDateTimeFormatAnnotationFormatterFactory());
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static-" + dispatcherProperties.getProperty("application.version") + "/**").addResourceLocations("/static/");
    }

    @Override
    public void addViewControllers(final ViewControllerRegistry registry) {
        registry.addViewController("/p/").setViewName("private");
        registry.addViewController("/config/").setViewName("config");
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/configError").setViewName("configError");
    }

}

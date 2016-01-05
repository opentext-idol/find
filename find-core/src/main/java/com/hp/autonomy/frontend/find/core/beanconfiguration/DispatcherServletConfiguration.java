/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.beanconfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.joda.JodaDateTimeFormatAnnotationFormatterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Component
public class DispatcherServletConfiguration extends WebMvcConfigurerAdapter {
    public static final String AUTHENTICATION_ERROR_PATH = "/authentication-error";
    public static final String CLIENT_AUTHENTICATION_ERROR_PATH = "/client-authentication-error";
    public static final String NOT_FOUND_ERROR_PATH = "/not-found-error";
    public static final String SERVER_ERROR_PATH = "/server-error";

    @Value("${application.commit}")
    private String commit;

    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    @Autowired(required = false)
    private Converter<?, ?>[] converters;

    @Override
    public void addFormatters(final FormatterRegistry registry) {
        if (converters != null) {
            for (final Converter<?, ?> converter : converters) {
                registry.addConverter(converter);
            }
        }

        registry.addFormatterForFieldAnnotation(new JodaDateTimeFormatAnnotationFormatterFactory());
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static-" + commit + "/**").addResourceLocations("classpath:/static/");
    }

    @Override
    public void addViewControllers(final ViewControllerRegistry registry) {
        registry.addViewController("/p/").setViewName("private");
        registry.addViewController("/config/").setViewName("config");
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/configError").setViewName("configError");
    }
}

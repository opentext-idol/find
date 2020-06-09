/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.beanconfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.joda.JodaDateTimeFormatAnnotationFormatterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
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
        if(converters != null) {
            for(final Converter<?, ?> converter : converters) {
                registry.addConverter(converter);
            }
        }

        registry.addFormatterForFieldAnnotation(new JodaDateTimeFormatAnnotationFormatterFactory());
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static-" + commit + "/**")
                .addResourceLocations("classpath:/static/");
    }
}

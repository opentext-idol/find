/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

@Configuration
public class FindTestConfiguration {
    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    @Autowired
    private WebMvcConfigurationSupport[] webMvcConfigurationSupports;


    @PostConstruct
    public void init() {
        final ServletContext servletContext = new MockServletContext();

        for (final WebMvcConfigurationSupport webMvcConfigurationSupport : webMvcConfigurationSupports){
            webMvcConfigurationSupport.setServletContext(servletContext);
        }
    }
}

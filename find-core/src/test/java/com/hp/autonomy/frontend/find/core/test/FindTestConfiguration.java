package com.hp.autonomy.frontend.find.core.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockServletContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

@Component
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

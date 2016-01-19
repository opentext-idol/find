/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.beanconfiguration;


import com.hp.autonomy.frontend.configuration.BaseConfigFileService;
import com.hp.autonomy.frontend.configuration.Config;
import com.hp.autonomy.frontend.configuration.ValidationService;
import com.hp.autonomy.frontend.configuration.ValidationServiceImpl;
import com.hp.autonomy.frontend.configuration.Validator;
import com.hp.autonomy.frontend.configuration.filter.ConfigEnvironmentVariableFilter;
import com.hp.autonomy.frontend.logging.ApplicationStartLogger;
import com.hp.autonomy.frontend.logging.UserLoggingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.ErrorPage;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;

/**
 * Contains beans useful in all configurations
 */
@Configuration
@PropertySource("classpath:/version.properties")
public class AppConfiguration<C extends Config<C>> {
    public static final String APPLICATION_RELEASE_VERSION_PROPERTY = "${application.releaseVersion}";
    public static final String GIT_COMMIT_PROPERTY = "${application.commit}";

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private BaseConfigFileService<C> configService;

    @SuppressWarnings("FieldMayBeFinal")
    @Autowired(required = false)
    private Set<Validator<?>> validators = Collections.emptySet();

    @SuppressWarnings("ReturnOfInnerClass")
    @Bean
    public EmbeddedServletContainerCustomizer containerCustomizer() {

        return new EmbeddedServletContainerCustomizer() {
            @Override
            public void customize(final ConfigurableEmbeddedServletContainer container) {

                final ErrorPage error401Page = new ErrorPage(HttpStatus.UNAUTHORIZED, DispatcherServletConfiguration.AUTHENTICATION_ERROR_PATH);
                final ErrorPage error403Page = new ErrorPage(HttpStatus.FORBIDDEN, DispatcherServletConfiguration.AUTHENTICATION_ERROR_PATH);
                final ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, DispatcherServletConfiguration.NOT_FOUND_ERROR_PATH);
                final ErrorPage error500Page = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, DispatcherServletConfiguration.SERVER_ERROR_PATH);

                container.addErrorPages(error401Page, error403Page, error404Page, error500Page);
            }
        };
    }

    /**
     * This is needed to force Tomcat to interpret POST bodies as UTF-8 by default, otherwise it'll use ISO-8859-1,
     * since that's apparently what the servlet spec specifies,
     * It's required despite URIEncoding="UTF-8" on the connector since that only works on GET parameters.
     * Jetty doesn't have this problem, it seems to use UTF-8 as the default.
     */
    @Bean
    public CharacterEncodingFilter characterEncodingFilter() {
        final CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");
        return characterEncodingFilter;
    }

    @Bean
    public FilterRegistrationBean userLoggingFilter() {
        final UserLoggingFilter userLoggingFilter = new UserLoggingFilter();
        userLoggingFilter.setUsePrincipal(true);

        final FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(userLoggingFilter);
        filterRegistrationBean.addUrlPatterns("/api/*");

        return filterRegistrationBean;
    }

    @Bean
    public ApplicationStartLogger applicationStartLogger() {
        return new ApplicationStartLogger();
    }

    @Bean
    public LocaleResolver localeResolver() {
        final SessionLocaleResolver sessionLocaleResolver = new SessionLocaleResolver();
        sessionLocaleResolver.setDefaultLocale(Locale.ENGLISH);

        return sessionLocaleResolver;
    }

    @Bean
    public ConfigEnvironmentVariableFilter configEnvironmentVariableFilter() {
        final ConfigEnvironmentVariableFilter configEnvironmentVariableFilter = new ConfigEnvironmentVariableFilter();
        configEnvironmentVariableFilter.setConfigPage("/configError");
        configEnvironmentVariableFilter.setConfigService(configService);

        return configEnvironmentVariableFilter;
    }

    @Bean
    @Autowired
    public ValidationService<C> validationService() {
        final ValidationServiceImpl<C> validationService = new ValidationServiceImpl<>();

        validationService.setValidators(validators);

        // fix circular dependency
        configService.setValidationService(validationService);

        return validationService;
    }
}

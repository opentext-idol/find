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

import com.hp.autonomy.frontend.configuration.BaseConfigFileService;
import com.hp.autonomy.frontend.configuration.validation.ValidationService;
import com.hp.autonomy.frontend.configuration.validation.ValidationServiceImpl;
import com.hp.autonomy.frontend.configuration.validation.Validator;
import com.hp.autonomy.frontend.find.core.configuration.FindConfig;
import com.hp.autonomy.frontend.find.core.logging.TomcatAccessLogValve;
import com.hp.autonomy.frontend.logging.ApplicationStartLogger;
import com.hp.autonomy.frontend.logging.UserLoggingFilter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.web.servlet.ErrorPage;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
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
public class AppConfiguration<C extends FindConfig<C, ?>> {
    public static final String APPLICATION_RELEASE_VERSION_PROPERTY = "${application.releaseVersion}";
    public static final String GIT_COMMIT_PROPERTY = "${application.commit}";
    public static final String SERVER_CONTEXT_PATH = "${server.context-path}";

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private BaseConfigFileService<C> configService;

    @SuppressWarnings("FieldMayBeFinal")
    @Autowired(required = false)
    private Set<Validator<?>> validators = Collections.emptySet();

    @SuppressWarnings("ReturnOfInnerClass")
    @Bean
    public EmbeddedServletContainerCustomizer containerCustomizer(
        @Value("${server.tomcat.accesslog.pattern:combined}") final String pattern
    ) {

        return container -> {
            final ErrorPage error401Page = new ErrorPage(HttpStatus.UNAUTHORIZED, DispatcherServletConfiguration.AUTHENTICATION_ERROR_PATH);
            final ErrorPage error403Page = new ErrorPage(HttpStatus.FORBIDDEN, DispatcherServletConfiguration.AUTHENTICATION_ERROR_PATH);
            final ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, DispatcherServletConfiguration.NOT_FOUND_ERROR_PATH);
            final ErrorPage error500Page = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, DispatcherServletConfiguration.SERVER_ERROR_PATH);

            container.addErrorPages(error401Page, error403Page, error404Page, error500Page);

            if (StringUtils.isNotEmpty(pattern) && container instanceof TomcatEmbeddedServletContainerFactory) {
                final TomcatAccessLogValve accessLogValve = new TomcatAccessLogValve();
                accessLogValve.setPattern(pattern);
                ((TomcatEmbeddedServletContainerFactory) container).addEngineValves(accessLogValve);
            }
        };
    }

    /**
     * This is needed to force Tomcat to interpret POST bodies as UTF-8 by default, otherwise it'll use ISO-8859-1,
     * since that's apparently what the servlet spec specifies,
     * It's required despite URIEncoding="UTF-8" on the connector since that only works on GET parameters.
     * Jetty doesn't have this problem, it seems to use UTF-8 as the default.
     * It also has to be a FilterRegistrationBean and be explicitly marked HIGHEST-PRECEDENCE otherwise it'll have no
     * effect if other filters run getParameter() before this filter is called.
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public FilterRegistrationBean characterEncodingFilter() {
        final CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");

        final FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(characterEncodingFilter);
        filterRegistrationBean.addUrlPatterns("/*");

        return filterRegistrationBean;
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
    public ValidationService<C> validationService() {
        final ValidationServiceImpl<C> validationService = new ValidationServiceImpl<>();

        validationService.setValidators(validators);

        // fix circular dependency
        configService.setValidationService(validationService);

        return validationService;
    }
}

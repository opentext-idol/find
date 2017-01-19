/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.metrics;

import com.autonomy.aci.client.transport.AciParameter;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.hp.autonomy.searchcomponents.idol.annotations.IdolService;
import com.hp.autonomy.searchcomponents.idol.exceptions.AciErrorExceptionAspect;
import com.hp.autonomy.types.requests.idol.actions.query.params.QueryParams;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Collection;
import java.util.stream.Collectors;

import static com.hp.autonomy.frontend.find.core.metrics.MetricsConfiguration.FIND_METRICS_PROPERTY;

/**
 * Default implementation of {@link AciErrorExceptionAspect}
 */
@SuppressWarnings("ProhibitedExceptionDeclared")
@Aspect
@Component
@ConditionalOnProperty(FIND_METRICS_PROPERTY)
class PerformanceMonitoringAspect {
    static final char METRIC_NAME_SEPARATOR = '.';
    static final String IDOL_REQUEST_METRIC_NAME_PREFIX = "idol" + METRIC_NAME_SEPARATOR;
    static final char CLASS_METHOD_SEPARATOR = ':';
    static final String PARAMETER_SEPARATOR = "&";
    static final char NAME_VALUE_SEPARATOR = '=';

    private final GaugeService gaugeService;

    @Autowired
    public PerformanceMonitoringAspect(final GaugeService gaugeService) {
        this.gaugeService = gaugeService;
    }

    @Around("@within(idolService)")
    public Object monitorServiceMethodPerformance(final ProceedingJoinPoint joinPoint, final IdolService idolService) throws Throwable {
        final String metricName = joinPoint.getSignature().getDeclaringTypeName() + CLASS_METHOD_SEPARATOR + joinPoint.getSignature().getName();
        return monitorMethodPerformance(joinPoint, metricName);
    }

    @Around(value = "execution(* com.autonomy.aci.client.transport.AciHttpClient.executeAction(..)) && args(serverDetails, parameters)",
            argNames = "joinPoint,serverDetails,parameters")
    public Object monitorIdolRequestPerformance(
            final ProceedingJoinPoint joinPoint,
            final AciServerDetails serverDetails,
            final Collection<? extends AciParameter> parameters) throws Throwable {
        final StringBuilder metricNameBuilder = new StringBuilder(IDOL_REQUEST_METRIC_NAME_PREFIX + serverDetails.getHost() + METRIC_NAME_SEPARATOR + serverDetails.getPort() + METRIC_NAME_SEPARATOR);
        final Collection<String> sortedParameters = parameters.stream()
                .filter(parameter -> !QueryParams.SecurityInfo.name().equalsIgnoreCase(parameter.getName()))
                .map(parameter -> parameter.getName() + NAME_VALUE_SEPARATOR + parameter.getValue())
                .sorted()
                .collect(Collectors.toList());
        metricNameBuilder.append(String.join(PARAMETER_SEPARATOR, sortedParameters));

        return monitorMethodPerformance(joinPoint, metricNameBuilder.toString());
    }

    private Object monitorMethodPerformance(final ProceedingJoinPoint joinPoint, final String metricName) throws Throwable {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            return joinPoint.proceed();
        } finally {
            stopWatch.stop();
            gaugeService.submit(metricName, stopWatch.getTotalTimeMillis());
        }
    }
}

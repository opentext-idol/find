/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.metrics;

import com.autonomy.aci.client.transport.AciParameter;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.searchcomponents.idol.annotations.IdolService;
import com.hp.autonomy.searchcomponents.idol.exceptions.AciErrorExceptionAspect;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Set;

/**
 * Default implementation of {@link AciErrorExceptionAspect}
 */
@SuppressWarnings("ProhibitedExceptionDeclared")
@Aspect
@Component
class PerformanceMonitoringAspect {
    private static final String IDOL_REQUEST_METRIC_NAME_PREFIX = "idol.";

    private final GaugeService gaugeService;

    @Autowired
    public PerformanceMonitoringAspect(final GaugeService gaugeService) {
        this.gaugeService = gaugeService;
    }

    @Around("@within(idolService)")
    public Object monitorServiceMethodPerformance(final ProceedingJoinPoint joinPoint, final IdolService idolService) throws Throwable {
        final String metricName = joinPoint.getSignature().getDeclaringTypeName() + ':' + joinPoint.getSignature().getName();
        return monitorMethodPerformance(joinPoint, metricName);
    }

    @Around(value = "execution(* com.autonomy.aci.client.transport.AciHttpClient.executeAction(..)) && args(serverDetails, parameters)",
            argNames = "joinPoint,serverDetails,parameters")
    public Object monitorIdolRequestPerformance(
            final ProceedingJoinPoint joinPoint,
            final AciServerDetails serverDetails,
            final Set<? extends AciParameter> parameters) throws Throwable {
        final String metricName = IDOL_REQUEST_METRIC_NAME_PREFIX + serverDetails.getHost() + '.' + serverDetails.getPort() + '.' + ((AciParameters) parameters).get("Action");
        return monitorMethodPerformance(joinPoint, metricName);
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

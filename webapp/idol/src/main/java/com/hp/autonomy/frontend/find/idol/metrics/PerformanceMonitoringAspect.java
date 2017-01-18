/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.metrics;

import com.hp.autonomy.searchcomponents.idol.annotations.IdolService;
import com.hp.autonomy.searchcomponents.idol.exceptions.AciErrorExceptionAspect;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

/**
 * Default implementation of {@link AciErrorExceptionAspect}
 */
@SuppressWarnings("ProhibitedExceptionDeclared")
@Aspect
@Component
class PerformanceMonitoringAspect {
    private final GaugeService gaugeService;

    @Autowired
    public PerformanceMonitoringAspect(final GaugeService gaugeService) {
        this.gaugeService = gaugeService;
    }

    @Around("@within(idolService)")
    public Object monitorServiceMethodPerformance(final ProceedingJoinPoint joinPoint, final IdolService idolService) throws Throwable {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            return joinPoint.proceed();
        } finally {
            stopWatch.stop();
            gaugeService.submit(joinPoint.getSignature().getDeclaringTypeName() + ':' + joinPoint.getSignature().getName(), stopWatch.getTotalTimeMillis());
        }
    }
}

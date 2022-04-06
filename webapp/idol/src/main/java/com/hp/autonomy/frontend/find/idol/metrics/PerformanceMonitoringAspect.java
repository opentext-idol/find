/*
 * (c) Copyright 2015 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.idol.metrics;

import com.autonomy.aci.client.transport.AciParameter;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.hp.autonomy.searchcomponents.idol.annotations.IdolService;
import com.hp.autonomy.searchcomponents.idol.exceptions.AciErrorExceptionAspect;
import com.hp.autonomy.types.requests.idol.actions.params.ActionParams;
import com.hp.autonomy.types.requests.idol.actions.query.params.QueryParams;
import com.hp.autonomy.types.requests.idol.actions.tags.params.GetQueryTagValuesParams;
import com.hp.autonomy.types.requests.idol.actions.user.UserActions;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.hp.autonomy.frontend.find.core.metrics.MetricsConfiguration.*;

/**
 * Default implementation of {@link AciErrorExceptionAspect}
 */
@SuppressWarnings("ProhibitedExceptionDeclared")
@Aspect
@Component
@ConditionalOnProperty(FIND_METRICS_ENABLED_PROPERTY_KEY)
class PerformanceMonitoringAspect {
    static final String SERVICE_METRIC_NAME_PREFIX = METRIC_NAME_SEPARATOR + "service" + METRIC_NAME_SEPARATOR;
    static final String IDOL_REQUEST_METRIC_NAME_PREFIX = METRIC_NAME_SEPARATOR + "idol" + METRIC_NAME_SEPARATOR;
    static final char CLASS_METHOD_SEPARATOR = ':';
    static final String PARAMETER_SEPARATOR = "&";
    static final char NAME_VALUE_SEPARATOR = '=';

    private static final Pattern ILLEGAL_COMPONENT_CHARACTERS = Pattern.compile("\\.");
    private static final Pattern ILLEGAL_CHARACTERS = Pattern.compile("[/\\\\]");
    private static final String ILLEGAL_CHARACTER_REPLACEMENT = "_";
    private static final String VALUE_PLACEHOLDER = "[any]";

    private final GaugeService gaugeService;
    private final String metricType;

    @Autowired
    public PerformanceMonitoringAspect(final GaugeService gaugeService,
                                       @Value(FIND_METRICS_TYPE_PROPERTY) final String metricType) {
        this.gaugeService = gaugeService;
        this.metricType = metricType;
    }

    @Around("@within(idolService)")
    public Object monitorServiceMethodPerformance(final ProceedingJoinPoint joinPoint, final IdolService idolService) throws Throwable {
        final String metricName = metricType
                + SERVICE_METRIC_NAME_PREFIX
                + sanitiseMetricNameComponent(joinPoint.getSignature().getDeclaringTypeName())
                + CLASS_METHOD_SEPARATOR
                + sanitiseMetricNameComponent(joinPoint.getSignature().getName());
        return monitorMethodPerformance(joinPoint, sanitiseMetricName(metricName));
    }

    @Around(value = "execution(* com.autonomy.aci.client.transport.AciHttpClient.executeAction(..)) && args(serverDetails, parameters)",
            argNames = "joinPoint,serverDetails,parameters")
    public Object monitorIdolRequestPerformance(
            final ProceedingJoinPoint joinPoint,
            final AciServerDetails serverDetails,
            final Collection<? extends AciParameter> parameters) throws Throwable {
        final StringBuilder metricNameBuilder = new StringBuilder(metricType)
                .append(IDOL_REQUEST_METRIC_NAME_PREFIX)
                .append(sanitiseMetricNameComponent(serverDetails.getHost()))
                .append(METRIC_NAME_SEPARATOR)
                .append(serverDetails.getPort())
                .append(METRIC_NAME_SEPARATOR);
        final Map<String, String> parameterMap = parameters.stream()
                .filter(parameter -> !QueryParams.SecurityInfo.name().equalsIgnoreCase(parameter.getName()) && StringUtils.isNotEmpty(parameter.getValue()))
                .collect(Collectors.toMap(AciParameter::getName, AciParameter::getValue));
        final Collection<String> parameterKeyValueStrings = parameterMap.entrySet().stream()
                .map(e -> e.getKey() + NAME_VALUE_SEPARATOR + sanitiseMetricNameComponent(tweakParameterValueInMetricName(e.getKey(), e.getValue())))
                .sorted()
                .collect(Collectors.toList());
        metricNameBuilder.append(String.join(PARAMETER_SEPARATOR, parameterKeyValueStrings));

        return Arrays.stream(UserActions.values()).anyMatch(a ->
                a.name().equalsIgnoreCase(parameterMap.get(ActionParams.Action.name())))
                ? joinPoint.proceed()
                : monitorMethodPerformance(joinPoint, sanitiseMetricName(metricNameBuilder.toString()));
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

    private String sanitiseMetricName(final CharSequence metricNameBuilder) {
        return ILLEGAL_CHARACTERS.matcher(metricNameBuilder).replaceAll(ILLEGAL_CHARACTER_REPLACEMENT);
    }

    private String sanitiseMetricNameComponent(final CharSequence metricNameBuilder) {
        return ILLEGAL_COMPONENT_CHARACTERS.matcher(metricNameBuilder).replaceAll(ILLEGAL_CHARACTER_REPLACEMENT);
    }

    private CharSequence tweakParameterValueInMetricName(final String name, final CharSequence value) {
        return QueryParams.Text.name().equalsIgnoreCase(name) && !"*".equals(value) || GetQueryTagValuesParams.Ranges.name().equalsIgnoreCase(name)
                ? VALUE_PLACEHOLDER
                : value;
    }
}

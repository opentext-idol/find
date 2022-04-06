/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.core.metrics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import static com.hp.autonomy.frontend.find.core.metrics.MetricsConfiguration.FIND_METRICS_ENABLED_PROPERTY_KEY;
import static com.hp.autonomy.frontend.find.core.metrics.MetricsConfiguration.FIND_METRICS_TYPE_PROPERTY;
import static com.hp.autonomy.frontend.find.core.metrics.MetricsConfiguration.METRIC_NAME_SEPARATOR;
import static com.hp.autonomy.frontend.find.core.metrics.MetricsController.PUBLIC_METRICS_PATH;

@Controller
@RequestMapping(PUBLIC_METRICS_PATH)
@ConditionalOnProperty(FIND_METRICS_ENABLED_PROPERTY_KEY)
class MetricsController {
    static final String PUBLIC_METRICS_PATH = "/api/public/metrics";
    static final String ADD_METRIC_PATH = "/add";
    static final String METRIC_NAME_PARAM = "metricName";
    static final String METRIC_VALUE_PARAM = "timeInMillis";
    static final String WEB_METRIC_PREFIX = METRIC_NAME_SEPARATOR + "web" + METRIC_NAME_SEPARATOR;

    private final GaugeService gaugeService;
    private final String metricType;

    @Autowired
    public MetricsController(final GaugeService gaugeService,
                             @Value(FIND_METRICS_TYPE_PROPERTY) final String metricType) {
        this.gaugeService = gaugeService;
        this.metricType = metricType;
    }

    @RequestMapping(value = ADD_METRIC_PATH, method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addMetric(@RequestParam(METRIC_NAME_PARAM) final String metricName,
                          @RequestParam(METRIC_VALUE_PARAM) final double metricValue) {
        gaugeService.submit(metricType + WEB_METRIC_PREFIX + metricName, metricValue);
    }
}

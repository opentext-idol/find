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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.MetricRepositoryAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static com.hp.autonomy.frontend.find.core.metrics.MetricsConfiguration.FIND_METRICS_ENABLED_PROPERTY_KEY;

/**
 * Customised copy of {@link MetricRepositoryAutoConfiguration}
 */
@Configuration
@ConditionalOnProperty(FIND_METRICS_ENABLED_PROPERTY_KEY)
public class MetricsConfiguration {
    public static final String FIND_METRICS_ENABLED_PROPERTY_KEY = "find.metrics.enabled";
    public static final char METRIC_NAME_SEPARATOR = '.';
    private static final boolean FIND_METRICS_ENABLED_DEFAULT = false;
    public static final String FIND_METRICS_ENABLED_PROPERTY = "${" + FIND_METRICS_ENABLED_PROPERTY_KEY + ':' + FIND_METRICS_ENABLED_DEFAULT + '}';
    private static final String FIND_METRICS_TYPE_PROPERTY_KEY = "find.metrics.type";
    private static final String FIND_METRICS_TYPE_DEFAULT = "timer";
    public static final String FIND_METRICS_TYPE_PROPERTY = "${" + FIND_METRICS_TYPE_PROPERTY_KEY + ':' + FIND_METRICS_TYPE_DEFAULT + '}';
    private static final String GRAPHITE_HOST_PROPERTY_KEY = "graphite.host";
    private static final String GRAPHITE_HOST_PROPERTY = "${" + GRAPHITE_HOST_PROPERTY_KEY + '}';
    private static final String GRAPHITE_PORT_PROPERTY_KEY = "graphite.port";
    private static final int GRAPHITE_PORT_DEFAULT = 2003;
    private static final String GRAPHITE_PORT_PROPERTY = "${" + GRAPHITE_PORT_PROPERTY_KEY + ':' + GRAPHITE_PORT_DEFAULT + '}';
    private static final String GRAPHITE_SCHEDULE_INTERVAL_PROPERTY_KEY = "graphite.schedule-interval";
    private static final int GRAPHITE_SCHEDULE_INTERVAL_DEFAULT = 1000;
    private static final String GRAPHITE_SCHEDULE_INTERVAL_PROPERTY = "${" + GRAPHITE_SCHEDULE_INTERVAL_PROPERTY_KEY + ':' + GRAPHITE_SCHEDULE_INTERVAL_DEFAULT + '}';
    private static final String GRAPHITE_PREFIX = "find";

    @Bean
    @ConditionalOnProperty(GRAPHITE_HOST_PROPERTY_KEY)
    public GraphiteReporter graphiteReporter(final MetricRegistry registry,
                                             @Value(GRAPHITE_HOST_PROPERTY) final String graphiteHost,
                                             @Value(GRAPHITE_PORT_PROPERTY) final int graphitePort,
                                             @Value(GRAPHITE_SCHEDULE_INTERVAL_PROPERTY) final int graphiteScheduleInterval) {
        final Graphite graphite = new Graphite(new InetSocketAddress(graphiteHost, graphitePort));
        final GraphiteReporter reporter = GraphiteReporter.forRegistry(registry)
            .prefixedWith(GRAPHITE_PREFIX)
            .build(graphite);
        reporter.start(graphiteScheduleInterval, TimeUnit.MILLISECONDS);
        return reporter;
    }
}

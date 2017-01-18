package com.hp.autonomy.frontend.find.core.metrics;

import org.springframework.boot.actuate.autoconfigure.ExportMetricReader;
import org.springframework.boot.actuate.autoconfigure.MetricRepositoryAutoConfiguration;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.actuate.metrics.buffer.BufferCounterService;
import org.springframework.boot.actuate.metrics.buffer.BufferMetricReader;
import org.springframework.boot.actuate.metrics.buffer.CounterBuffers;
import org.springframework.boot.actuate.metrics.buffer.GaugeBuffers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Customised copy of {@link MetricRepositoryAutoConfiguration}
 */
@Configuration
public class MetricsConfiguration {
    @Bean
    public CounterBuffers counterBuffers() {
        return new CounterBuffers();
    }

    @Bean
    public GaugeBuffers gaugeBuffers() {
        return new GaugeBuffers();
    }

    @Bean
    @ExportMetricReader
    public BufferMetricReader actuatorMetricReader(final CounterBuffers counters,
                                                   final GaugeBuffers gauges) {
        return new BufferMetricReader(counters, gauges) {
            @Override
            public Iterable<Metric<?>> findAll() {
                final Iterable<Metric<?>> metrics = super.findAll();
                return StreamSupport.stream(metrics.spliterator(), false).sorted((x, y) -> x.getName().compareTo(y.getName())).collect(Collectors.toList());
            }
        };
    }

    @Bean
    public BufferCounterService counterService(final CounterBuffers writer) {
        return new BufferCounterService(writer);
    }
}

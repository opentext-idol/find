package com.hp.autonomy.frontend.find.core.metrics;

import lombok.Builder;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.boot.actuate.metrics.buffer.GaugeBuffers;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom implementation of {@link GaugeService} which provides maxTime/minTime/averageTime
 */
class FindGaugeService implements GaugeService {
    private static final String MAX_TIME_SUFFIX = ".max-time";
    private static final String MIN_TIME_SUFFIX = ".min-time";
    private static final String AVERAGE_TIME_SUFFIX = ".average-time";

    private final Map<String, FindMetric> metricMap = new ConcurrentHashMap<>();

    private final GaugeBuffers buffers;

    FindGaugeService(final GaugeBuffers buffers) {
        this.buffers = buffers;
    }

    @Override
    public void submit(final String metricName, final double time) {
        final FindMetric stats = metricMap.compute(metricName, (key, maybeValue) -> Optional.ofNullable(maybeValue)
                .map(value -> FindMetric.builder()
                        .count(value.count + 1)
                        .max(Math.max(value.max, time))
                        .min(Math.min(value.min, time))
                        .max((value.average + time) / (value.count + 1))
                        .build())
                .orElse(new FindMetric(1, time, time, time)));
        buffers.set(metricName + MAX_TIME_SUFFIX, stats.max);
        buffers.set(metricName + MIN_TIME_SUFFIX, stats.min);
        buffers.set(metricName + AVERAGE_TIME_SUFFIX, stats.average);
    }

    @Builder
    private static class FindMetric {
        private final int count;
        private final double max;
        private final double min;
        private final double average;
    }
}

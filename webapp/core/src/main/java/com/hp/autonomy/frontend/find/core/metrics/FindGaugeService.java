package com.hp.autonomy.frontend.find.core.metrics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.boot.actuate.metrics.buffer.GaugeBuffers;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Custom implementation of {@link GaugeService} which provides maxTime/minTime/averageTime
 */
@Component
public class FindGaugeService implements GaugeService {
    private static final String MAX_TIME_SUFFIX = ".max-time";
    private static final String MIN_TIME_SUFFIX = ".min-time";
    private static final String AVERAGE_TIME_SUFFIX = ".average-time";

    private final Map<String, Collection<Double>> metricMap = new ConcurrentHashMap<>();

    private final GaugeBuffers buffers;

    @Autowired
    public FindGaugeService(final GaugeBuffers buffers) {
        this.buffers = buffers;
    }

    @Override
    public void submit(final String metricName, final double value) {
        metricMap.putIfAbsent(metricName, new ArrayList<>());
        final Collection<Double> times = metricMap.get(metricName);
        times.add(value);
        final DoubleSummaryStatistics stats = new ArrayList<>(times).stream().collect(Collectors.summarizingDouble(Double::doubleValue));
        buffers.set(metricName + MAX_TIME_SUFFIX, stats.getMax());
        buffers.set(metricName + MIN_TIME_SUFFIX, stats.getMin());
        buffers.set(metricName + AVERAGE_TIME_SUFFIX, stats.getAverage());
    }
}

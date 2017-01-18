package com.hp.autonomy.frontend.find.core.metrics;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.actuate.metrics.buffer.BufferMetricReader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MetricsConfiguration.class, FindGaugeService.class})
public class FindGaugeServiceTest {
    @Autowired
    private GaugeService gaugeService;
    @Autowired
    private BufferMetricReader metricReader;

    @Test
    public void submit() {
        final String metricName = "someName";
        final double metricValue = 0.1;
        gaugeService.submit(metricName, metricValue);
        final Iterable<Metric<?>> metrics = metricReader.findAll();
        assertThat(metrics, hasItem(hasProperty("name", startsWith("someName"))));
    }
}

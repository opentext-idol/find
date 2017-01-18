package com.hp.autonomy.frontend.find.core.metrics;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.boot.actuate.metrics.buffer.GaugeBuffers;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MetricsConfiguration.class, FindGaugeService.class})
public class FindGaugeServiceTest {
    @Autowired
    private GaugeService gaugeService;
    @MockBean
    private GaugeBuffers buffers;

    @Test
    public void submit() {
        gaugeService.submit("someName", 0.1);
        verify(buffers, times(3)).set(anyString(), eq(0.1));
    }
}

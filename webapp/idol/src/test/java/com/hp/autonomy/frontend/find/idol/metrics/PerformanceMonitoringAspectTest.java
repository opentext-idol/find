package com.hp.autonomy.frontend.find.idol.metrics;

import com.hp.autonomy.searchcomponents.idol.annotations.IdolService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit4.SpringRunner;

import static com.hp.autonomy.frontend.find.idol.metrics.PerformanceMonitoringAspectTest.UNIQUE_PROPERTY;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {PerformanceMonitoringAspect.class, PerformanceMonitoringAspectTest.TestConfiguration.class},
        properties = UNIQUE_PROPERTY,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class PerformanceMonitoringAspectTest {
    static final String UNIQUE_PROPERTY = "performance-monitor-aspect-test";

    @MockBean
    private GaugeService gaugeService;
    @Autowired
    private TestService testService;

    @Test
    public void monitorServiceMethodPerformance() {
        testService.testMethod();
        verify(gaugeService).submit(anyString(), anyDouble());
    }

    @IdolService
    static class TestService {
        void testMethod() {
        }
    }

    @Configuration
    @ConditionalOnProperty(UNIQUE_PROPERTY)
    @EnableAspectJAutoProxy
    static class TestConfiguration {
        @Bean
        TestService testService() {
            return new TestService();
        }
    }
}

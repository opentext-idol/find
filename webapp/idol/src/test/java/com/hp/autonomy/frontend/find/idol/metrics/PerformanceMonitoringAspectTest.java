package com.hp.autonomy.frontend.find.idol.metrics;

import com.autonomy.aci.client.transport.AciHttpClient;
import com.autonomy.aci.client.transport.AciHttpException;
import com.autonomy.aci.client.transport.AciParameter;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.searchcomponents.idol.annotations.IdolService;
import com.hp.autonomy.types.requests.idol.actions.params.ActionParams;
import com.hp.autonomy.types.requests.idol.actions.query.QueryActions;
import com.hp.autonomy.types.requests.idol.actions.query.params.QueryParams;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Set;

import static com.hp.autonomy.frontend.find.core.metrics.MetricsConfiguration.*;
import static com.hp.autonomy.frontend.find.idol.metrics.PerformanceMonitoringAspect.*;
import static com.hp.autonomy.frontend.find.idol.metrics.PerformanceMonitoringAspectTest.UNIQUE_PROPERTY;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {PerformanceMonitoringAspect.class, PerformanceMonitoringAspectTest.TestConfiguration.class},
        properties = {UNIQUE_PROPERTY, FIND_METRICS_ENABLED_PROPERTY_KEY},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class PerformanceMonitoringAspectTest {
    static final String UNIQUE_PROPERTY = "performance-monitor-aspect-test";

    @MockBean
    private GaugeService gaugeService;
    @Autowired
    private TestService testService;
    @Autowired
    private AciHttpClient aciHttpClient;
    @Value(FIND_METRICS_TYPE_PROPERTY)
    private String metricType;

    @Test
    public void monitorServiceMethodPerformance() {
        testService.testMethod();
        final String expectedMetricName = metricType
                + SERVICE_METRIC_NAME_PREFIX
                + "com_hp_autonomy_frontend_find_idol_metrics_PerformanceMonitoringAspectTest$TestService"
                + CLASS_METHOD_SEPARATOR
                + "testMethod";
        verify(gaugeService).submit(eq(expectedMetricName), anyDouble());
    }

    @SuppressWarnings("resource")
    @Test
    public void monitorIdolRequestPerformance() throws IOException, AciHttpException {
        final Set<AciParameter> parameters = new AciParameters(QueryActions.Query.name());
        final String text = "*";
        parameters.add(new AciParameter(QueryParams.Text.name(), text));
        final String database = "Test";
        parameters.add(new AciParameter(QueryParams.DatabaseMatch.name(), database));

        final String host = "localhost";
        final int port = 5678;
        aciHttpClient.executeAction(new AciServerDetails(host, port), parameters);
        final String expectedMetricName =
                metricType
                        + IDOL_REQUEST_METRIC_NAME_PREFIX
                        + host + METRIC_NAME_SEPARATOR
                        + port + METRIC_NAME_SEPARATOR
                        + ActionParams.Action.name() + NAME_VALUE_SEPARATOR + QueryActions.Query.name()
                        + PARAMETER_SEPARATOR + QueryParams.DatabaseMatch.name() + NAME_VALUE_SEPARATOR + database
                        + PARAMETER_SEPARATOR + QueryParams.Text.name() + NAME_VALUE_SEPARATOR + text;
        verify(gaugeService).submit(eq(expectedMetricName), anyDouble());
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

        @Bean
        AciHttpClient aciHttpClient() {
            return mock(AciHttpClient.class);
        }
    }
}

package com.hp.autonomy.frontend.find.idol.controlpoint;

import com.hp.autonomy.frontend.find.idol.configuration.ControlPointConfig;
import com.hp.autonomy.frontend.find.idol.configuration.ControlPointServerConfig;
import com.hp.autonomy.frontend.find.idol.configuration.CredentialsConfig;
import org.apache.http.client.HttpClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

public class ControlPointServiceTest {
    private HttpClient httpClient;
    private ControlPointConfig config;
    private ControlPointService service;

    @Before
    public void setUp() {
        httpClient = Mockito.mock(HttpClient.class);
        config = ControlPointConfig.builder()
            .enabled(true)
            .server(ControlPointServerConfig.builder()
                .protocol("http")
                .host("cp-host")
                .port(123)
                .credentials(CredentialsConfig.builder()
                    .username("cp-user")
                    .password("cp-pass")
                    .build())
                .build())
            .build();
        service = new ControlPointService(httpClient, config);
    }

    @Test
    public void testApplyPolicy() throws IOException, ControlPointApiException {
        Mockito.when(httpClient.execute(Mockito.any()))
            .thenReturn(ControlPointApiClientTest.buildLoginResponse())
            .thenReturn(ControlPointApiClientTest.buildResponse(200, "OK",
                "{\"Success\":true,\"ItemsWillBeIgnored\":false}"));
        // expect no exception thrown
        service.applyPolicy("the policy", "state token", null);
    }

    @Test
    public void testApplyPolicy_returnsFailure() throws IOException, ControlPointApiException {
        Mockito.when(httpClient.execute(Mockito.any()))
            .thenReturn(ControlPointApiClientTest.buildLoginResponse())
            .thenReturn(ControlPointApiClientTest.buildResponse(200, "OK",
                "{\"Success\":false,\"ItemsWillBeIgnored\":false}"));
        // expect no exception thrown
        service.applyPolicy("the policy", "state token", null);
    }

    @Test
    public void testApplyPolicy_partialSuccess() throws IOException, ControlPointApiException {
        Mockito.when(httpClient.execute(Mockito.any()))
            .thenReturn(ControlPointApiClientTest.buildLoginResponse())
            .thenReturn(ControlPointApiClientTest.buildResponse(200, "OK",
                "{\"Success\":true,\"ItemsWillBeIgnored\":true}"));
        // expect no exception thrown
        service.applyPolicy("the policy", "state token", null);
    }

}

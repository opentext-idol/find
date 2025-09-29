package com.hp.autonomy.frontend.find.idol.controlpoint;

import com.hp.autonomy.frontend.find.idol.configuration.ControlPointConfig;
import com.hp.autonomy.frontend.find.idol.configuration.ControlPointServerConfig;
import com.hp.autonomy.frontend.find.idol.configuration.CredentialsConfig;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
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
        Mockito.doAnswer(ControlPointApiClientTest.buildLoginAnswer())
                .doAnswer(ControlPointApiClientTest.buildAnswer(200,
                        "{\"Success\":true,\"ItemsWillBeIgnored\":false}"))
                .when(httpClient).execute(Mockito.any(), Mockito.<HttpClientResponseHandler<?>>any());
        // expect no exception thrown
        service.applyPolicy("the policy", "state token", null);
    }

    @Test
    public void testApplyPolicy_returnsFailure() throws IOException, ControlPointApiException {
        Mockito.doAnswer(ControlPointApiClientTest.buildLoginAnswer())
                .doAnswer(ControlPointApiClientTest.buildAnswer(200,
                        "{\"Success\":false,\"ItemsWillBeIgnored\":false}"))
                .when(httpClient).execute(Mockito.any(), Mockito.<HttpClientResponseHandler<?>>any());
        // expect no exception thrown
        service.applyPolicy("the policy", "state token", null);
    }

    @Test
    public void testApplyPolicy_partialSuccess() throws IOException, ControlPointApiException {
        Mockito.doAnswer(ControlPointApiClientTest.buildLoginAnswer())
                .doAnswer(ControlPointApiClientTest.buildAnswer(200,
                        "{\"Success\":true,\"ItemsWillBeIgnored\":true}"))
                .when(httpClient).execute(Mockito.any(), Mockito.<HttpClientResponseHandler<?>>any());
        // expect no exception thrown
        service.applyPolicy("the policy", "state token", null);
    }

}

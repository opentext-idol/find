package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import com.autonomy.aci.client.services.impl.AciServiceImpl;
import com.autonomy.aci.client.transport.AciHttpClient;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.hp.autonomy.frontend.configuration.authentication.CommunityAuthentication;
import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import org.flywaydb.core.Flyway;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class FlywayIdolConfigUpdateHandlerTest {
    @MockBean
    private ProcessorFactory processorFactory;

    @MockBean
    private Flyway flyway;

    @MockBean
    private AciHttpClient aciHttpClient;

    @SuppressWarnings("rawtypes")
    @Mock
    private ValidationResult validationResult;

    @Mock
    private AciServerDetails serverDetails;

    @Mock
    private IdolFindConfig config;

    @Mock
    private CommunityAuthentication community;

    private FlywayIdolConfigUpdateHandler flywayIdolConfigUpdateHandler;

    @Before
    public void setUp() {
        flywayIdolConfigUpdateHandler = new FlywayIdolConfigUpdateHandler(processorFactory, flyway, aciHttpClient);

        when(config.getLogin()).thenReturn(community);
        when(config.getCommunityDetails()).thenReturn(serverDetails);
        when(community.getMethod()).thenReturn("autonomy");
        //noinspection unchecked
        when(community.validate(Matchers.any(AciServiceImpl.class), Matchers.any(ProcessorFactory.class))).thenReturn(validationResult);
        when(validationResult.isValid()).thenReturn(true);

        when(serverDetails.getProtocol()).thenReturn(AciServerDetails.TransportProtocol.HTTP);
        when(serverDetails.getHost()).thenReturn("communityHost");
        when(serverDetails.getPort()).thenReturn(9000);
    }

    @Test
    public void updateWithValidConfig() {
        flywayIdolConfigUpdateHandler.update(config);
        Mockito.verify(flyway).migrate();
    }

    @Test(expected = RuntimeException.class)
    public void updateWithInvalidConfig() {
        when(validationResult.isValid()).thenReturn(false);
        flywayIdolConfigUpdateHandler.update(config);
        Mockito.verify(flyway, Mockito.never()).migrate();
    }

    @Test
    public void updateWithInitialConfig() {
        when(community.getMethod()).thenReturn("default");
        flywayIdolConfigUpdateHandler.update(config);
        Mockito.verify(flyway, Mockito.never()).migrate();
    }

}

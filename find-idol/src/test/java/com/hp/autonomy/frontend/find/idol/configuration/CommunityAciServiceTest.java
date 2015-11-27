package com.hp.autonomy.frontend.find.idol.configuration;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.hp.autonomy.frontend.configuration.ConfigService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommunityAciServiceTest {
    @Mock
    private AciServerDetails communityDetails;

    @Mock
    private IdolFindConfig idolFindConfig;

    @Mock
    private ConfigService<IdolFindConfig> configService;

    @Mock
    private AciService aciService;

    private CommunityAciService communityAciService;

    @Before
    public void setUp() {
        when(idolFindConfig.getCommunityDetails()).thenReturn(communityDetails);
        when(configService.getConfig()).thenReturn(idolFindConfig);
        communityAciService = new CommunityAciService(aciService, configService);
    }

    @Test
    public void getServerDetails() {
        assertNotNull(communityAciService.getServerDetails());
    }
}

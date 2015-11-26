package com.hp.autonomy.frontend.find.idol.indexes;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.frontend.find.idol.aci.AciResponseProcessorFactory;
import com.hp.autonomy.types.idol.Database;
import com.hp.autonomy.types.idol.Databases;
import com.hp.autonomy.types.idol.GetStatusResponseData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IdolIndexesServiceTest {
    @Mock
    private AciService contentAciService;

    @Mock
    private AciResponseProcessorFactory aciResponseProcessorFactory;

    private IdolIndexesService idolIndexesService;

    @Before
    public void setUp() {
        idolIndexesService = new IdolIndexesService(contentAciService, aciResponseProcessorFactory);
    }

    @Test
    public void listVisibleIndexes() {
        final GetStatusResponseData responseData = new GetStatusResponseData();
        final Databases databases = new Databases();
        databases.getDatabase().add(new Database());
        responseData.setDatabases(databases);
        when(contentAciService.executeAction(any(AciParameters.class), any(Processor.class))).thenReturn(responseData);

        assertThat(idolIndexesService.listVisibleIndexes(), is(not(empty())));
    }
}

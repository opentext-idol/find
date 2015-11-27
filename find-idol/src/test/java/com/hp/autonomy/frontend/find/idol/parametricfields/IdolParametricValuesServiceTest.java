package com.hp.autonomy.frontend.find.idol.parametricfields;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciParameter;
import com.hp.autonomy.frontend.find.idol.aci.AciResponseProcessorFactory;
import com.hp.autonomy.idol.parametricvalues.IdolParametricRequest;
import com.hp.autonomy.types.idol.FlatField;
import com.hp.autonomy.types.idol.GetQueryTagValuesResponseData;
import com.hp.autonomy.types.idol.TagValue;
import com.hp.autonomy.types.requests.idol.actions.tags.QueryTagInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IdolParametricValuesServiceTest {
    @Mock
    private AciService contentAciService;

    @Mock
    private AciResponseProcessorFactory aciResponseProcessorFactory;

    @Mock
    private JAXBElement<? extends Serializable> element;

    private IdolParametricValuesService parametricValuesService;

    @Before
    public void setUp() {
        parametricValuesService = new IdolParametricValuesService(contentAciService, aciResponseProcessorFactory);
    }

    @Test
    public void getAllParametricValues() {
        final IdolParametricRequest idolParametricRequest = new IdolParametricRequestBuilder().buildRequest(Collections.<String>emptySet(), Collections.singleton("Some field"), "*", "");

        final GetQueryTagValuesResponseData responseData = mockQueryResponse();
        when(contentAciService.executeAction(anySetOf(AciParameter.class), any(Processor.class))).thenReturn(responseData);
        final Set<QueryTagInfo> results = parametricValuesService.getAllParametricValues(idolParametricRequest);
        assertThat(results, is(not(empty())));
    }

    private GetQueryTagValuesResponseData mockQueryResponse() {
        final GetQueryTagValuesResponseData responseData = new GetQueryTagValuesResponseData();
        final FlatField field = new FlatField();
        field.getName().add("Some name");
        when(element.getName()).thenReturn(new QName("", "value"));
        final TagValue tagValue = new TagValue();
        tagValue.setValue("Some field");
        tagValue.setCount(5);
        when(element.getValue()).thenReturn(tagValue);
        field.getValueOrSubvalueOrValues().add(element);
        responseData.getField().add(field);
        return responseData;
    }
}

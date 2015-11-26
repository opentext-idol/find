package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciParameter;
import com.hp.autonomy.frontend.configuration.ProductType;
import com.hp.autonomy.frontend.find.core.search.FindDocument;
import com.hp.autonomy.frontend.find.core.search.FindQueryParams;
import com.hp.autonomy.frontend.find.idol.aci.AciResponseProcessorFactory;
import com.hp.autonomy.frontend.find.idol.aci.DatabaseName;
import com.hp.autonomy.types.idol.DocContent;
import com.hp.autonomy.types.idol.GetVersionResponseData;
import com.hp.autonomy.types.idol.Hit;
import com.hp.autonomy.types.idol.QueryResponseData;
import com.hp.autonomy.types.idol.SuggestResponseData;
import com.hp.autonomy.types.requests.Documents;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IdolDocumentServiceTest {
    @Mock
    private AciService contentAciService;

    @Mock
    private AciResponseProcessorFactory aciResponseProcessorFactory;

    private IdolDocumentService idolDocumentService;

    @Before
    public void setUp() {
        idolDocumentService = new IdolDocumentService(contentAciService, aciResponseProcessorFactory);
    }

    @Test
    public void queryTextIndex() {
        final QueryResponseData responseData = mockQueryResponse();

        when(contentAciService.executeAction(anySetOf(AciParameter.class), any(Processor.class))).thenReturn(responseData);

        final Documents<FindDocument> results = idolDocumentService.queryTextIndex(mockQueryParams());
        assertThat(results.getDocuments(), is(not(empty())));
    }

    @Test
    public void queryContentForPromotions() {


        mockResponsesForPromotions(ProductType.AXE);
        idolDocumentService = new IdolDocumentService(contentAciService, aciResponseProcessorFactory);

        final Documents<FindDocument> results = idolDocumentService.queryTextIndexForPromotions(mockQueryParams());
        assertThat(results.getDocuments(), is(empty()));
    }

    @Test
    public void queryQmsForPromotions() {
        mockResponsesForPromotions(ProductType.QMS);
        idolDocumentService = new IdolDocumentService(contentAciService, aciResponseProcessorFactory);

        final Documents<FindDocument> results = idolDocumentService.queryTextIndexForPromotions(mockQueryParams());
        assertThat(results.getDocuments(), is(not(empty())));
    }

    @Test
    public void findSimilar() {
        final SuggestResponseData responseData = new SuggestResponseData();
        responseData.setTotalhits(1);
        final Hit hit = mockHit();
        responseData.getHit().add(hit);

        when(contentAciService.executeAction(anySetOf(AciParameter.class), any(Processor.class))).thenReturn(responseData);

        final List<FindDocument> results = idolDocumentService.findSimilar(Collections.<DatabaseName>emptySet(), "Some reference");
        assertThat(results, is(not(empty())));
    }

    private FindQueryParams<DatabaseName> mockQueryParams() {
        final FindQueryParams<DatabaseName> queryParams = new FindQueryParams<>();
        queryParams.setText("*");
        queryParams.setIndex(Arrays.asList(new DatabaseName("Databse1"), new DatabaseName("Database2")));
        return queryParams;
    }

    private QueryResponseData mockQueryResponse() {
        final QueryResponseData responseData = new QueryResponseData();
        responseData.setTotalhits(1);
        final Hit hit = mockHit();
        responseData.getHit().add(hit);

        return responseData;
    }

    private void mockResponsesForPromotions(final ProductType productType) {
        final GetVersionResponseData versionResponseData = new GetVersionResponseData();
        versionResponseData.setProducttypecsv(productType.name());

        final QueryResponseData queryResponseData = mockQueryResponse();

        when(contentAciService.executeAction(anySetOf(AciParameter.class), any(Processor.class))).thenReturn(versionResponseData).thenReturn(queryResponseData);
    }

    private Hit mockHit() {
        final Hit hit = new Hit();
        hit.setTitle("Some Title");

        final DocContent content = new DocContent();
        final Element element = mock(Element.class);
        final NodeList childNodes = mock(NodeList.class);
        when(childNodes.getLength()).thenReturn(1);
        final Element childNode = mock(Element.class);
        final Node textNode = mock(Node.class);
        when(textNode.getNodeValue()).thenReturn("Some Value");
        when(childNode.getFirstChild()).thenReturn(textNode);
        when(childNodes.item(0)).thenReturn(childNode);
        when(element.getElementsByTagName(anyString())).thenReturn(childNodes);
        content.getContent().add(element);
        hit.setContent(content);

        return hit;
    }
}

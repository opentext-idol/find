package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciParameter;
import com.autonomy.aci.client.transport.AciResponseInputStream;
import com.hp.autonomy.frontend.configuration.ProductType;
import com.hp.autonomy.frontend.find.core.search.FindDocument;
import com.hp.autonomy.frontend.find.core.search.FindQueryParams;
import com.hp.autonomy.frontend.find.idol.aci.AciResponseProcessorCallback;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
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

    @Mock
    private Processor<Documents<FindDocument>> queryResponseProcessor;

    @Mock
    private Processor<List<FindDocument>> suggestResponseProcessor;

    @Mock
    private Processor<List<String>> versionResponseProcessor;

    @Mock
    private AciResponseInputStream aciResponseInputStream;

    private IdolDocumentService idolDocumentService;

    @Before
    public void setUp() {
        when(contentAciService.executeAction(anySetOf(AciParameter.class), any(Processor.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(final InvocationOnMock invocationOnMock) {
                //noinspection unchecked
                return ((Processor<?>) invocationOnMock.getArguments()[1]).process(aciResponseInputStream);
            }
        });
    }

    @Test
    public void queryTextIndex() {
        final Deque<AciResponseProcessorCallback<?, ?>> callbacks = new ArrayDeque<>(1);
        when(aciResponseProcessorFactory.createAciResponseProcessor(any(Class.class), any(AciResponseProcessorCallback.class))).thenAnswer(new Answer<Processor<?>>() {
            @Override
            public Processor<?> answer(final InvocationOnMock invocationOnMock) {
                //noinspection unchecked
                callbacks.add((AciResponseProcessorCallback<?, ?>) invocationOnMock.getArguments()[1]);
                return queryResponseProcessor;
            }
        });

        mockQueryResponseProcessor(callbacks);

        idolDocumentService = new IdolDocumentService(contentAciService, aciResponseProcessorFactory);

        final Documents<FindDocument> results = idolDocumentService.queryTextIndex(new FindQueryParams<DatabaseName>());
        assertThat(results.getDocuments(), is(not(empty())));
    }

    @Test
    public void queryContentForPromotions() {
        mockProcessorsForPromotions(ProductType.AXE);
        idolDocumentService = new IdolDocumentService(contentAciService, aciResponseProcessorFactory);

        final Documents<FindDocument> results = idolDocumentService.queryTextIndexForPromotions(new FindQueryParams<DatabaseName>());
        assertThat(results.getDocuments(), is(empty()));
    }

    @Test
    public void queryQmsForPromotions() {
        mockProcessorsForPromotions(ProductType.QMS);
        idolDocumentService = new IdolDocumentService(contentAciService, aciResponseProcessorFactory);

        final Documents<FindDocument> results = idolDocumentService.queryTextIndexForPromotions(new FindQueryParams<DatabaseName>());
        assertThat(results.getDocuments(), is(not(empty())));
    }

    @Test
    public void findSimilar() {
        final Deque<AciResponseProcessorCallback<?, ?>> callbacks = new ArrayDeque<>(1);
        when(aciResponseProcessorFactory.createAciResponseProcessor(any(Class.class), any(AciResponseProcessorCallback.class))).thenReturn(null).thenAnswer(new Answer<Processor<?>>() {
            @Override
            public Processor<?> answer(final InvocationOnMock invocationOnMock) {
                //noinspection unchecked
                callbacks.add((AciResponseProcessorCallback<?, ?>) invocationOnMock.getArguments()[1]);
                return suggestResponseProcessor;
            }
        }).thenReturn(null);

        when(suggestResponseProcessor.process(aciResponseInputStream)).thenAnswer(new Answer<List<FindDocument>>() {
            @Override
            public List<FindDocument> answer(final InvocationOnMock invocationOnMock) {
                final SuggestResponseData responseData = new SuggestResponseData();
                responseData.setTotalhits(1);
                final Hit hit = mockHit();
                responseData.getHit().add(hit);
                //noinspection unchecked
                return ((AciResponseProcessorCallback<SuggestResponseData, List<FindDocument>>) callbacks.pop()).process(responseData);
            }
        });

        idolDocumentService = new IdolDocumentService(contentAciService, aciResponseProcessorFactory);

        final List<FindDocument> results = idolDocumentService.findSimilar(Collections.<DatabaseName>emptySet(), "Some reference");
        assertThat(results, is(not(empty())));
    }

    private void mockQueryResponseProcessor(final Deque<AciResponseProcessorCallback<?, ?>> callbacks) {
        when(queryResponseProcessor.process(aciResponseInputStream)).thenAnswer(new Answer<Documents<FindDocument>>() {
            @Override
            public Documents<FindDocument> answer(final InvocationOnMock invocationOnMock) {
                final QueryResponseData responseData = new QueryResponseData();
                responseData.setTotalhits(1);
                final Hit hit = mockHit();
                responseData.getHit().add(hit);
                //noinspection unchecked
                return ((AciResponseProcessorCallback<QueryResponseData, Documents<FindDocument>>) callbacks.pop()).process(responseData);
            }
        });
    }

    private void mockProcessorsForPromotions(final ProductType productType) {
        final Deque<AciResponseProcessorCallback<?, ?>> callbacks = new ArrayDeque<>(3);
        final Deque<Processor<?>> processors = new ArrayDeque<>(3);
        processors.add(queryResponseProcessor);
        processors.add(versionResponseProcessor);

        when(aciResponseProcessorFactory.createAciResponseProcessor(any(Class.class), any(AciResponseProcessorCallback.class)))
                .thenAnswer(new Answer<Processor<?>>() {
                    @Override
                    public Processor<?> answer(final InvocationOnMock invocationOnMock) {
                        //noinspection unchecked
                        callbacks.push((AciResponseProcessorCallback<?, ?>) invocationOnMock.getArguments()[1]);
                        return processors.pop();
                    }
                })
                .thenReturn(null)
                .thenAnswer(new Answer<Processor<?>>() {
                    @Override
                    public Processor<?> answer(final InvocationOnMock invocationOnMock) {
                        //noinspection unchecked
                        callbacks.push((AciResponseProcessorCallback<?, ?>) invocationOnMock.getArguments()[1]);
                        return processors.pop();
                    }
                });

        when(versionResponseProcessor.process(aciResponseInputStream)).thenAnswer(new Answer<List<String>>() {
            @Override
            public List<String> answer(final InvocationOnMock invocationOnMock) {
                final GetVersionResponseData responseData = new GetVersionResponseData();
                responseData.setProducttypecsv(productType.name());
                //noinspection unchecked
                return ((AciResponseProcessorCallback<GetVersionResponseData, List<String>>) callbacks.pop()).process(responseData);
            }
        });

        mockQueryResponseProcessor(callbacks);
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

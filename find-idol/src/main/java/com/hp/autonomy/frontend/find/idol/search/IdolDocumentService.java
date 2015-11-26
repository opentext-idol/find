package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciParameter;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.frontend.configuration.ProductType;
import com.hp.autonomy.frontend.find.core.search.DocumentsService;
import com.hp.autonomy.frontend.find.core.search.FindDocument;
import com.hp.autonomy.frontend.find.core.search.FindQueryParams;
import com.hp.autonomy.frontend.find.idol.aci.AciResponseProcessorCallback;
import com.hp.autonomy.frontend.find.idol.aci.AciResponseProcessorFactory;
import com.hp.autonomy.frontend.find.idol.aci.DatabaseName;
import com.hp.autonomy.types.idol.GetVersionResponseData;
import com.hp.autonomy.types.idol.Hit;
import com.hp.autonomy.types.idol.QueryResponseData;
import com.hp.autonomy.types.idol.SuggestResponseData;
import com.hp.autonomy.types.requests.Documents;
import com.hp.autonomy.types.requests.idol.actions.general.GeneralActions;
import com.hp.autonomy.types.requests.idol.actions.query.QueryActions;
import com.hp.autonomy.types.requests.idol.actions.query.params.PrintParam;
import com.hp.autonomy.types.requests.idol.actions.query.params.QueryParams;
import com.hp.autonomy.types.requests.idol.actions.query.params.SuggestParams;
import com.hp.autonomy.types.requests.idol.actions.query.params.SummaryParam;
import com.hp.autonomy.types.requests.qms.QmsActionParams;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class IdolDocumentService implements DocumentsService<DatabaseName, FindDocument, AciErrorException> {
    private final AciService contentAciService;
    private final Processor<Documents<FindDocument>> queryResponseProcessor;
    private final Processor<List<FindDocument>> suggestResponseProcessor;
    private final Processor<List<String>> versionResponseProcessor;

    @Autowired
    public IdolDocumentService(final AciService contentAciService, final AciResponseProcessorFactory aciResponseProcessorFactory) {
        this.contentAciService = contentAciService;

        queryResponseProcessor = aciResponseProcessorFactory.createAciResponseProcessor(QueryResponseData.class, new AciResponseProcessorCallback<QueryResponseData, Documents<FindDocument>>() {
            @Override
            public Documents<FindDocument> process(final QueryResponseData responseData) {
                final List<Hit> hits = responseData.getHit();
                final List<FindDocument> results = parseQueryHits(hits);
                return new Documents<>(results, responseData.getTotalhits(), null);
            }
        });

        suggestResponseProcessor = aciResponseProcessorFactory.createAciResponseProcessor(SuggestResponseData.class, new AciResponseProcessorCallback<SuggestResponseData, List<FindDocument>>() {
            @Override
            public List<FindDocument> process(final SuggestResponseData responseData) {
                final List<Hit> hits = responseData.getHit();
                return parseQueryHits(hits);
            }
        });

        versionResponseProcessor = aciResponseProcessorFactory.createAciResponseProcessor(GetVersionResponseData.class, new AciResponseProcessorCallback<GetVersionResponseData, List<String>>() {
            @Override
            public List<String> process(final GetVersionResponseData responseData) {
                return Arrays.asList(responseData.getProducttypecsv().split(","));
            }
        });
    }

    @Override
    public Documents<FindDocument> queryTextIndex(final FindQueryParams<DatabaseName> findQueryParams) throws AciErrorException {
        return queryTextIndex(findQueryParams, false);
    }

    @Override
    public Documents<FindDocument> queryTextIndexForPromotions(final FindQueryParams<DatabaseName> findQueryParams) throws AciErrorException {
        final Set<AciParameter> aciParameters = new AciParameters(GeneralActions.GetVersion.name());
        final List<String> productTypes = contentAciService.executeAction(aciParameters, versionResponseProcessor);

        return productTypes.contains(ProductType.QMS.name()) ? queryTextIndex(findQueryParams, true) : new Documents<>(Collections.<FindDocument>emptyList(), 0, null);
    }

    private Documents<FindDocument> queryTextIndex(final FindQueryParams<DatabaseName> findQueryParams, final boolean qms) {
        final Set<AciParameter> aciParameters = new AciParameters(QueryActions.Query.name());
        aciParameters.add(new AciParameter(QueryParams.MaxResults.name(), findQueryParams.getMaxResults()));
        aciParameters.add(new AciParameter(QueryParams.Summary.name(), SummaryParam.fromValue(findQueryParams.getSummary(), null)));
        aciParameters.add(new AciParameter(QueryParams.DatabaseMatch.name(), convertCollectionToIdolCsv(findQueryParams.getIndex())));
        aciParameters.add(new AciParameter(QueryParams.FieldText.name(), findQueryParams.getFieldText()));
        aciParameters.add(new AciParameter(QueryParams.Sort.name(), findQueryParams.getSort()));
        aciParameters.add(new AciParameter(QueryParams.MinDate.name(), findQueryParams.getMinDate()));
        aciParameters.add(new AciParameter(QueryParams.MaxDate.name(), findQueryParams.getMaxDate()));
        aciParameters.add(new AciParameter(QueryParams.Print.name(), PrintParam.Fields));
        aciParameters.add(new AciParameter(QueryParams.PrintFields.name(), FindDocument.ALL_FIELDS));
        if (qms) {
//        aciParameters.add(new AciParameter(QmsActionParams.Blacklist.name(), )); TODO
//        aciParameters.add(new AciParameter(QmsActionParams.ExpandQuery.name(), )); TODO
            aciParameters.add(new AciParameter(QmsActionParams.Promotions.name(), true));
        }

        return contentAciService.executeAction(aciParameters, queryResponseProcessor);
    }

    private String convertCollectionToIdolCsv(final Collection<?> collection) {
        return collection == null ? null : StringUtils.join(collection.toArray(), '+');
    }

    @Override
    public List<FindDocument> findSimilar(final Set<DatabaseName> indexes, final String reference) throws AciErrorException {
        final Set<AciParameter> aciParameters = new AciParameters(QueryActions.Suggest.name());
        aciParameters.add(new AciParameter(SuggestParams.Reference.name(), reference));
        aciParameters.add(new AciParameter(SuggestParams.DatabaseMatch.name(), convertCollectionToIdolCsv(indexes)));

        return contentAciService.executeAction(aciParameters, suggestResponseProcessor);
    }

    private List<FindDocument> parseQueryHits(final Collection<Hit> hits) {
        final List<FindDocument> results = new ArrayList<>(hits.size());
        for (final Hit hit : hits) {
            final Element docContent = (Element) hit.getContent().getContent().get(0);

            final FindDocument.Builder findDocumentBuilder = new FindDocument.Builder()
                    .setReference(hit.getReference())
                    .setIndex(hit.getDatabase())
                    .setTitle(hit.getTitle())
                    .setSummary(hit.getSummary())
                    .setDate(hit.getDatestring(), hit.getDate())
                    .setContentType(parseFields(docContent, FindDocument.CONTENT_TYPE_FIELD))
                    .setUrl(parseFields(docContent, FindDocument.URL_FIELD))
                    .setAuthors(parseFields(docContent, FindDocument.AUTHOR_FIELD))
                    .setCategories(parseFields(docContent, FindDocument.CATEGORY_FIELD))
                    .setDateCreated(parseFields(docContent, FindDocument.DATE_CREATED_FIELD))
                    .setCreatedDate(parseFields(docContent, FindDocument.CREATED_DATE_FIELD))
                    .setModifiedDate(parseFields(docContent, FindDocument.MODIFIED_DATE_FIELD))
                    .setDateModified(parseFields(docContent, FindDocument.DATE_MODIFIED_FIELD));
            results.add(findDocumentBuilder.build());
        }
        return results;
    }

    private List<String> parseFields(final Element node, final String fieldName) {
        final NodeList childNodes = node.getElementsByTagName(fieldName);
        final int length = childNodes.getLength();
        final List<String> values = new ArrayList<>(length);

        for (int i = 0; i < length; i++) {
            final Node childNode = childNodes.item(i);
            values.add(childNode.getFirstChild().getNodeValue());
        }

        return values;
    }
}

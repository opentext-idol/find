package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciParameter;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.aci.content.database.Databases;
import com.hp.autonomy.aci.content.identifier.reference.Reference;
import com.hp.autonomy.frontend.configuration.ProductType;
import com.hp.autonomy.frontend.find.core.search.DocumentsService;
import com.hp.autonomy.frontend.find.core.search.FindDocument;
import com.hp.autonomy.frontend.find.core.search.FindQueryParams;
import com.hp.autonomy.frontend.find.idol.aci.AciResponseProcessorFactory;
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
public class IdolDocumentService implements DocumentsService<String, FindDocument, AciErrorException> {
    private final AciService contentAciService;
    private final Processor<QueryResponseData> queryResponseProcessor;
    private final Processor<SuggestResponseData> suggestResponseProcessor;
    private final Processor<GetVersionResponseData> versionResponseProcessor;

    @Autowired
    public IdolDocumentService(final AciService contentAciService, final AciResponseProcessorFactory aciResponseProcessorFactory) {
        this.contentAciService = contentAciService;

        queryResponseProcessor = aciResponseProcessorFactory.createAciResponseProcessor(QueryResponseData.class);
        suggestResponseProcessor = aciResponseProcessorFactory.createAciResponseProcessor(SuggestResponseData.class);
        versionResponseProcessor = aciResponseProcessorFactory.createAciResponseProcessor(GetVersionResponseData.class);
    }

    @Override
    public Documents<FindDocument> queryTextIndex(final FindQueryParams<String> findQueryParams) throws AciErrorException {
        return queryTextIndex(findQueryParams, false);
    }

    @Override
    public Documents<FindDocument> queryTextIndexForPromotions(final FindQueryParams<String> findQueryParams) throws AciErrorException {
        final Set<AciParameter> aciParameters = new AciParameters(GeneralActions.GetVersion.name());

        final GetVersionResponseData versionResponseData = contentAciService.executeAction(aciParameters, versionResponseProcessor);
        final List<String> productTypes = Arrays.asList(versionResponseData.getProducttypecsv().split(","));

        return productTypes.contains(ProductType.QMS.name()) ? queryTextIndex(findQueryParams, true) : new Documents<>(Collections.<FindDocument>emptyList(), 0, null);
    }

    private Documents<FindDocument> queryTextIndex(final FindQueryParams<String> findQueryParams, final boolean qms) {
        final AciParameters aciParameters = new AciParameters(QueryActions.Query.name());
        aciParameters.add(QueryParams.Text.name(), findQueryParams.getText());
        aciParameters.add(QueryParams.MaxResults.name(), findQueryParams.getMaxResults());
        aciParameters.add(QueryParams.Summary.name(), SummaryParam.fromValue(findQueryParams.getSummary(), null));
        aciParameters.add(QueryParams.DatabaseMatch.name(), new Databases(findQueryParams.getIndex()));
        aciParameters.add(QueryParams.FieldText.name(), findQueryParams.getFieldText());
        aciParameters.add(QueryParams.Sort.name(), findQueryParams.getSort());
        aciParameters.add(QueryParams.MinDate.name(), findQueryParams.getMinDate());
        aciParameters.add(QueryParams.MaxDate.name(), findQueryParams.getMaxDate());
        aciParameters.add(QueryParams.Print.name(), PrintParam.Fields);
        aciParameters.add(QueryParams.PrintFields.name(), FindDocument.ALL_FIELDS);
        aciParameters.add(QueryParams.XMLMeta.name(), true);
//        aciParameters.add(QmsActionParams.Blacklist.name(), ); TODO
//        aciParameters.add(QmsActionParams.ExpandQuery.name(), ); TODO

        if (qms) {
            aciParameters.add(QmsActionParams.Promotions.name(), true);
        }

        final QueryResponseData responseData = contentAciService.executeAction(aciParameters, queryResponseProcessor);
        final List<Hit> hits = responseData.getHit();
        final List<FindDocument> results = parseQueryHits(hits);
        return new Documents<>(results, responseData.getTotalhits(), null);
    }

    @Override
    public List<FindDocument> findSimilar(final Set<String> indexes, final String reference) throws AciErrorException {
        final AciParameters aciParameters = new AciParameters(QueryActions.Suggest.name());
        aciParameters.add(SuggestParams.Reference.name(), new Reference(reference));
        aciParameters.add(SuggestParams.DatabaseMatch.name(), new Databases(indexes));

        final SuggestResponseData responseData = contentAciService.executeAction(aciParameters, suggestResponseProcessor);
        final List<Hit> hits = responseData.getHit();
        return parseQueryHits(hits);
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
                    .setDate(hit.getDatestring())
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

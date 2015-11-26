package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.frontend.find.core.search.RelatedConceptsService;
import com.hp.autonomy.frontend.find.idol.aci.AciResponseProcessorCallback;
import com.hp.autonomy.frontend.find.idol.aci.AciResponseProcessorFactory;
import com.hp.autonomy.frontend.find.idol.aci.DatabaseName;
import com.hp.autonomy.types.idol.QsElement;
import com.hp.autonomy.types.idol.QueryResponseData;
import com.hp.autonomy.types.requests.idol.actions.query.QueryActions;
import com.hp.autonomy.types.requests.idol.actions.query.params.PrintParam;
import com.hp.autonomy.types.requests.idol.actions.query.params.QueryParams;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class IdolRelatedConceptsService implements RelatedConceptsService<QsElement, DatabaseName, AciErrorException> {
    private final AciService contentAciService;
    private final Processor<List<QsElement>> queryResponseProcessor;

    @Autowired
    public IdolRelatedConceptsService(final AciService contentAciService, final AciResponseProcessorFactory aciResponseProcessorFactory) {
        this.contentAciService = contentAciService;
        queryResponseProcessor = aciResponseProcessorFactory.createAciResponseProcessor(QueryResponseData.class, new AciResponseProcessorCallback<QueryResponseData, List<QsElement>>() {
            @Override
            public List<QsElement> process(final QueryResponseData responseData) {
                return responseData.getQs() != null ? responseData.getQs().getElement() : Collections.<QsElement>emptyList();
            }
        });
    }

    @Override
    public List<QsElement> findRelatedConcepts(final String text, final List<DatabaseName> indexes, final String fieldText) throws AciErrorException {
        final AciParameters parameters = new AciParameters(QueryActions.Query.name());
        parameters.add(QueryParams.Text.name(), text);
        parameters.add(QueryParams.DatabaseMatch.name(), convertCollectionToIdolCsv(indexes));
        parameters.add(QueryParams.FieldText.name(), fieldText);
        parameters.add(QueryParams.MaxResults.name(), 50); //TODO
        parameters.add(QueryParams.Print.name(), PrintParam.NoResults);
        parameters.add(QueryParams.QuerySummary.name(), true);
        return contentAciService.executeAction(parameters, queryResponseProcessor);
    }

    private String convertCollectionToIdolCsv(final Collection<?> collection) {
        return collection == null ? null : StringUtils.join(collection.toArray(), '+');
    }
}

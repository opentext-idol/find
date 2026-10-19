/*
 * Copyright 2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.idol.entitysearch;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.util.ActionParameters;
import com.hp.autonomy.aci.content.database.Databases;
import com.hp.autonomy.aci.content.printfields.PrintFields;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.idol.api.EntitySearchApi;
import com.hp.autonomy.frontend.find.idol.api.model.IdolSearchResultsResponse;
import com.hp.autonomy.frontend.find.idol.configuration.EntitySearchConfig;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.searchcomponents.idol.search.HavenSearchAciParameterHandler;
import com.hp.autonomy.searchcomponents.idol.search.QueryResponseParser;
import com.hp.autonomy.types.requests.idol.actions.query.QueryActions;
import com.hp.autonomy.types.requests.idol.actions.query.params.QueryParams;
import com.opentext.idol.types.marshalling.ProcessorFactory;
import com.opentext.idol.types.responses.QueryResponseData;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@RestController
class EntitySearchController implements EntitySearchApi {
    private final HavenSearchAciParameterHandler parameterHandler;
    private final Processor<QueryResponseData> queryResponseProcessor;
    private final QueryResponseParser queryResponseParser;
    private final AciService entitySearchAciService;
    private final ConfigService<IdolFindConfig> configService;

    @Autowired
    EntitySearchController(final HavenSearchAciParameterHandler parameterHandler,
                           final ProcessorFactory processorFactory,
                           final AciService entitySearchAciService,
                           final ConfigService<IdolFindConfig> configService,
                           final QueryResponseParser queryResponseParser) {
        this.parameterHandler = parameterHandler;
        this.queryResponseProcessor = processorFactory.getResponseDataProcessor(QueryResponseData.class);
        this.entitySearchAciService = entitySearchAciService;
        this.configService = configService;

        this.queryResponseParser = queryResponseParser;
    }

    @Override
    public ResponseEntity<IdolSearchResultsResponse> searchEntities(
            final String text,
            final String databaseGroup
    ) {
        ActionParameters aciParameters = new ActionParameters(QueryActions.Query.name());

        final EntitySearchConfig entitySearch = configService.getConfig().getEntitySearch();

        if (!BooleanUtils.isTrue(entitySearch.getEnabled())) {
            throw new IllegalArgumentException("Entity search is disabled");
        }

        aciParameters.add(QueryParams.AgentBooleanField.name(), entitySearch.getAgentBooleanField());
        aciParameters.add(QueryParams.Combine.name(), entitySearch.getCombine());
        aciParameters.add(QueryParams.IgnoreSpecials.name(), true);
        aciParameters.add(QueryParams.MaxResults.name(), 1);
        final Collection<String> printFields = entitySearch.getIdolPrintFields();
        aciParameters.add(QueryParams.PrintFields.name(), CollectionUtils.isEmpty(printFields) ? "*" : new PrintFields(printFields));
        aciParameters.add(QueryParams.Text.name(), text);
        aciParameters.add(QueryParams.AbsWeight.name(), entitySearch.getAbsWeight());

        if (StringUtils.isNotBlank(databaseGroup) && entitySearch.getDatabaseChoices() != null) {
            final List<String> dbNames = entitySearch.getDatabaseChoices().get(databaseGroup);

            if (dbNames != null) {
                aciParameters.add(QueryParams.DatabaseMatch.name(), new Databases(dbNames));
            }
        }

        this.parameterHandler.addSecurityInfo(aciParameters);

        final QueryResponseData resp = this.entitySearchAciService.executeAction(aciParameters, this.queryResponseProcessor);
        return ResponseEntity.ok(
                new IdolSearchResultsResponse(this.queryResponseParser.parseQueryHits(resp.getHits(), null)));
    }
}

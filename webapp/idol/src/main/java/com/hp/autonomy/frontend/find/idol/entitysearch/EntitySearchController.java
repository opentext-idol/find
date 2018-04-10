/*
 * Copyright 2017 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.entitysearch;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.aci.content.database.Databases;
import com.hp.autonomy.aci.content.printfields.PrintFields;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.idol.configuration.EntitySearchConfig;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.searchcomponents.idol.search.HavenSearchAciParameterHandler;
import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;
import com.hp.autonomy.searchcomponents.idol.search.QueryResponseParser;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import com.hp.autonomy.types.idol.responses.QueryResponseData;
import com.hp.autonomy.types.requests.idol.actions.query.QueryActions;
import com.hp.autonomy.types.requests.idol.actions.query.params.QueryParams;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(EntitySearchController.BASE_PATH)
class EntitySearchController {
    static final String BASE_PATH = "/api/public/entitysearch";
    static final String SEARCH_PATH = "search";
    static final String TEXT_PARAM = "text";
    static final String DATABASE_GROUP_PARAM = "databaseGroup";

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

    @RequestMapping(value = SEARCH_PATH, method = RequestMethod.GET)
    public List<IdolSearchResult> search(
            @RequestParam(TEXT_PARAM) final String text,
            @RequestParam(value = DATABASE_GROUP_PARAM, required = false) final String databaseGroup
    ) {
        AciParameters aciParameters = new AciParameters(QueryActions.Query.name());

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
        return this.queryResponseParser.parseQueryHits(resp.getHits());
    }
}

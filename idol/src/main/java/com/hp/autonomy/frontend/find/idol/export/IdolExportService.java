/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.export;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.frontend.find.core.export.ExportFormat;
import com.hp.autonomy.frontend.find.core.export.ExportService;
import com.hp.autonomy.frontend.find.core.export.ExportStrategy;
import com.hp.autonomy.searchcomponents.core.search.SearchRequest;
import com.hp.autonomy.searchcomponents.idol.configuration.AciServiceRetriever;
import com.hp.autonomy.searchcomponents.idol.search.HavenSearchAciParameterHandler;
import com.hp.autonomy.types.requests.idol.actions.query.QueryActions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.util.EnumMap;
import java.util.Map;

@Component
public class IdolExportService implements ExportService<String, AciErrorException> {
    private final HavenSearchAciParameterHandler parameterHandler;
    private final AciServiceRetriever aciServiceRetriever;
    private final Map<ExportFormat, ExportStrategy> exportStrategies;

    @Autowired
    public IdolExportService(final HavenSearchAciParameterHandler parameterHandler,
                             final AciServiceRetriever aciServiceRetriever,
                             final ExportStrategy[] exportStrategies) {
        this.parameterHandler = parameterHandler;
        this.aciServiceRetriever = aciServiceRetriever;

        this.exportStrategies = new EnumMap<>(ExportFormat.class);
        for (final ExportStrategy exportStrategy : exportStrategies) {
            this.exportStrategies.put(exportStrategy.getExportFormat(), exportStrategy);
        }
    }

    @Override
    public void export(final OutputStream outputStream, final SearchRequest<String> searchRequest, final ExportFormat exportFormat) throws AciErrorException {
        final AciParameters aciParameters = new AciParameters(QueryActions.Query.name());

        parameterHandler.addSearchRestrictions(aciParameters, searchRequest.getQueryRestrictions());
        parameterHandler.addSearchOutputParameters(aciParameters, searchRequest);
        if (searchRequest.getQueryType() != SearchRequest.QueryType.RAW) {
            parameterHandler.addQmsParameters(aciParameters, searchRequest.getQueryRestrictions());
        }

        final ExportStrategy exportStrategy = exportStrategies.get(exportFormat);
        aciServiceRetriever.getAciService(searchRequest.getQueryType()).executeAction(aciParameters, new ExportQueryResponseProcessor(exportStrategy, outputStream));
    }
}

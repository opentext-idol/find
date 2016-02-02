/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.indexes;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.frontend.find.core.indexes.IndexesService;
import com.hp.autonomy.idolutils.processors.AciResponseJaxbProcessorFactory;
import com.hp.autonomy.types.idol.Database;
import com.hp.autonomy.types.idol.GetStatusResponseData;
import com.hp.autonomy.types.requests.idol.actions.status.StatusActions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class IdolIndexesService implements IndexesService<Database, AciErrorException> {
    private final AciService contentAciService;
    private final Processor<GetStatusResponseData> responseProcessor;

    @Autowired
    public IdolIndexesService(final AciService contentAciService, final AciResponseJaxbProcessorFactory aciResponseProcessorFactory) {
        this.contentAciService = contentAciService;

        responseProcessor = aciResponseProcessorFactory.createAciResponseProcessor(GetStatusResponseData.class);
    }

    @Override
    public Set<Database> listVisibleIndexes() throws AciErrorException {
        final GetStatusResponseData responseData = contentAciService.executeAction(new AciParameters(StatusActions.GetStatus.name()), responseProcessor);
        final List<Database> allDatabases = responseData.getDatabases().getDatabase();
        final Set<Database> publicDatabases = new LinkedHashSet<>(allDatabases.size());
        for (final Database database : allDatabases) {
            if (!database.isInternal()) {
                publicDatabases.add(database);
            }
        }

        return publicDatabases;
    }
}

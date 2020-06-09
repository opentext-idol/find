/*
 * (c) Copyright 2015 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */
package com.hp.autonomy.frontend.find.hod.search;

import com.hp.autonomy.frontend.find.core.search.DocumentsController;
import com.hp.autonomy.hod.client.api.resource.ResourceName;
import com.hp.autonomy.hod.client.api.textindex.query.search.Print;
import com.hp.autonomy.hod.client.error.HodError;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.search.GetContentRequestBuilder;
import com.hp.autonomy.searchcomponents.hod.search.HodDocumentsService;
import com.hp.autonomy.searchcomponents.hod.search.HodGetContentRequest;
import com.hp.autonomy.searchcomponents.hod.search.HodGetContentRequestBuilder;
import com.hp.autonomy.searchcomponents.hod.search.HodGetContentRequestIndex;
import com.hp.autonomy.searchcomponents.hod.search.HodGetContentRequestIndexBuilder;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRequest;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRequestBuilder;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictions;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictionsBuilder;
import com.hp.autonomy.searchcomponents.hod.search.HodSearchResult;
import com.hp.autonomy.searchcomponents.hod.search.HodSuggestRequest;
import com.hp.autonomy.searchcomponents.hod.search.HodSuggestRequestBuilder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(DocumentsController.SEARCH_PATH)
class HodDocumentsController extends DocumentsController<HodQueryRequest, HodSuggestRequest, HodGetContentRequest, ResourceName, HodQueryRestrictions, HodGetContentRequestIndex, HodSearchResult, HodErrorException> {
    @SuppressWarnings({"TypeMayBeWeakened", "ConstructorWithTooManyParameters"})
    @Autowired
    public HodDocumentsController(final HodDocumentsService documentsService,
                                  final ObjectFactory<HodQueryRestrictionsBuilder> queryRestrictionsBuilderFactory,
                                  final ObjectFactory<HodQueryRequestBuilder> queryRequestBuilderFactory,
                                  final ObjectFactory<HodSuggestRequestBuilder> suggestRequestBuilderFactory,
                                  final ObjectFactory<HodGetContentRequestBuilder> getContentRequestBuilderFactory,
                                  final ObjectFactory<HodGetContentRequestIndexBuilder> getContentRequestIndexBuilderFactory) {
        super(documentsService, queryRestrictionsBuilderFactory, queryRequestBuilderFactory, suggestRequestBuilderFactory, getContentRequestBuilderFactory, getContentRequestIndexBuilderFactory);
    }

    @Override
    protected <T> T throwException(final String message) throws HodErrorException {
        throw new HodErrorException(new HodError.Builder().setMessage(message).build(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Override
    protected void addParams(final GetContentRequestBuilder<HodGetContentRequest, HodGetContentRequestIndex, ?> request) {
        ((HodGetContentRequestBuilder) request)
                .print(Print.all);
    }
}

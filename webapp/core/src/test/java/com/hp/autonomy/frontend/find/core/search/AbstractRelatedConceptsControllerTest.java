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

package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.search.RelatedConceptsRequest;
import com.hp.autonomy.searchcomponents.core.search.RelatedConceptsService;
import com.hp.autonomy.types.requests.idol.actions.query.QuerySummaryElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Serializable;
import java.util.Collections;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractRelatedConceptsControllerTest<T extends QuerySummaryElement, Q extends QueryRestrictions<S>, R extends RelatedConceptsRequest<Q>, S extends Serializable, E extends Exception> {
    private RelatedConceptsService<R, T, Q, E> relatedConceptsService;
    private RelatedConceptsController<T, Q, R, S, E> relatedConceptsController;

    protected abstract RelatedConceptsController<T, Q, R, S, E> buildController();

    protected abstract RelatedConceptsService<R, T, Q, E> buildService();

    @Before
    public void setUp() {
        relatedConceptsController = buildController();
        relatedConceptsService = buildService();
    }

    @Test
    public void query() throws E {
        relatedConceptsController.findRelatedConcepts("Some query text", null, Collections.emptyList(), null, null, 0, null, null, null, "MODIFIED");
        verify(relatedConceptsService).findRelatedConcepts(Matchers.any());
    }
}

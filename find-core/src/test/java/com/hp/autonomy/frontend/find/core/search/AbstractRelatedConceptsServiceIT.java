/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import com.hp.autonomy.types.requests.idol.actions.query.QuerySummaryElement;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public abstract class AbstractRelatedConceptsServiceIT<Q extends QuerySummaryElement, S extends Serializable, E extends Exception> extends AbstractFindIT {
    @Autowired
    protected RelatedConceptsController<Q, S, E> relatedConceptsController;

    protected final List<S> indexes;

    protected AbstractRelatedConceptsServiceIT(final List<S> indexes) {
        this.indexes = new ArrayList<>(indexes);
    }

    @Test
    public void findRelatedConcepts() throws E {
        final List<Q> results = relatedConceptsController.findRelatedConcepts("*", indexes, "");
        assertThat(results, is(not(empty())));
    }
}

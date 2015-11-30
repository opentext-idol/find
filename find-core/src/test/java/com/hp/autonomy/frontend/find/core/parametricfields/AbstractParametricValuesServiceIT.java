/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.parametricfields;

import com.hp.autonomy.core.parametricvalues.ParametricRequest;
import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import com.hp.autonomy.types.requests.idol.actions.tags.QueryTagInfo;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class AbstractParametricValuesServiceIT<R extends ParametricRequest<S>, S extends Serializable, E extends Exception> extends AbstractFindIT {
    @Autowired
    private ParametricValuesController<R, S, E> parametricValuesController;

    protected final Set<S> indexes;
    protected final Set<String> fieldNames;

    public AbstractParametricValuesServiceIT(final List<S> indexes, final Set<String> fieldNames) {
        this.fieldNames = new HashSet<>(fieldNames);
        this.indexes = new HashSet<>(indexes);
    }

    @Test
    public void getParametricValues() throws E {
        final Set<QueryTagInfo> results = parametricValuesController.getParametricValues(indexes, fieldNames, "*", "");
        assertThat(results, is(empty())); // TODO: configure this later
    }
}

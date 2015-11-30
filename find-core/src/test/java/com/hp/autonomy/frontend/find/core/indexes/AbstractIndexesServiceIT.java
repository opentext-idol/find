/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.indexes;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import com.hp.autonomy.types.IdolDatabase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public abstract class AbstractIndexesServiceIT<D extends IdolDatabase, E extends Exception> extends AbstractFindIT {
    @Autowired
    protected ListIndexesController<D, E> listIndexesController;

    @Test
    public void noExcludedIndexes() throws E {
        final List<D> databases = listIndexesController.listActiveIndexes();
        assertThat(databases, is(not(empty())));
    }
}

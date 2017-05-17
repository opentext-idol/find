/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.web;

import com.hp.autonomy.searchcomponents.core.search.QueryRequest;

import java.io.IOException;

@FunctionalInterface
public interface RequestMapper<R extends QueryRequest<?>> {
    R parseQueryRequest(String json) throws IOException;
}

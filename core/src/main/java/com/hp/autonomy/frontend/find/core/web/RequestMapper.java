/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.web;

import com.hp.autonomy.searchcomponents.core.search.SearchRequest;

import java.io.IOException;
import java.io.Serializable;

public interface RequestMapper<S extends Serializable> {
    SearchRequest<S> parseSearchRequest(String json) throws IOException;
}

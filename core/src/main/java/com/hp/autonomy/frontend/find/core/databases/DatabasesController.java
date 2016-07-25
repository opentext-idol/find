/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.databases;

import com.hp.autonomy.searchcomponents.core.databases.DatabasesRequest;
import com.hp.autonomy.searchcomponents.core.databases.DatabasesService;
import com.hp.autonomy.types.IdolDatabase;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Set;

@Controller
public abstract class DatabasesController<D extends IdolDatabase, R extends DatabasesRequest, E extends Exception> {
    public static final String GET_DATABASES_PATH = "/api/public/search/list-indexes";

    private final DatabasesService<D, R, E> databasesService;

    protected DatabasesController(final DatabasesService<D, R, E> databasesService) {
        this.databasesService = databasesService;
    }

    @RequestMapping(value = GET_DATABASES_PATH, method = RequestMethod.GET)
    @ResponseBody
    public Set<D> getDatabases() throws E {
        return databasesService.getDatabases(buildDatabasesRequest());
    }

    protected abstract R buildDatabasesRequest();
}

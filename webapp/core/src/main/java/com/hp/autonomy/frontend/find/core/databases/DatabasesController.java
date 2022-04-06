/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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

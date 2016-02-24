/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches.query;

import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping(SavedQueryController.PATH)
public class SavedQueryController {
    public static final String PATH = "/api/public/saved-query";

    private final SavedSearchService<SavedQuery> service;

    @Autowired
    public SavedQueryController(final SavedQueryService service) {
        this.service = service;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Set<SavedQuery> getAll() {
        return service.getAll();
    }

    @RequestMapping(method = RequestMethod.POST)
    public SavedQuery create(
            @RequestBody final SavedQuery query
    ) {
        return service.create(query);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.PUT)
    public SavedQuery update(
            @PathVariable("id") final long id,
            @RequestBody final SavedQuery query
    ) {
        return service.update(
                new SavedQuery.Builder(query).setId(id).build()
        );
    }

    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public void delete(
            @PathVariable("id") final long id
    ) {
        service.deleteById(id);
    }
}

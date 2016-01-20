/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/public/saved-query")
public class SavedQueryController<I> {
    private final SavedQueryService<I> service;

    @Autowired
    public SavedQueryController(final SavedQueryService<I> service) {
        this.service = service;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Set<SavedQuery<I>> getAll() {
        return service.getAll();
    }

    @RequestMapping(method = RequestMethod.POST)
    public SavedQuery<I> create(
            @RequestBody final SavedQuery<I> query
    ) {
        return service.create(query);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.PATCH)
    public SavedQuery<I> update(
            @PathVariable("id") final int id,
            @RequestBody final SavedQuery<I> query
    ) {
        return service.update(
                new SavedQuery.Builder<>(query).setId(id).build()
        );
    }

    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public void delete(
            @PathVariable("id") final int id
    ) {
        service.deleteById(id);
    }
}

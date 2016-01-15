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
@RequestMapping("/api/public/saved-search")
public class SavedSearchController<I> {
    private final SavedSearchService<I> service;

    @Autowired
    public SavedSearchController(final SavedSearchService<I> service) {
        this.service = service;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Set<SavedSearch<I>> getAll() {
        return service.getAll();
    }

    @RequestMapping(method = RequestMethod.POST)
    public SavedSearch<I> create(
            @RequestBody final SavedSearch<I> search
    ) {
        return service.create(search);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.PATCH)
    public SavedSearch<I> update(
            @PathVariable("id") final long id,
            @RequestBody final SavedSearch<I> search
    ) {
        return service.update(
                new SavedSearch.Builder<>(search).setId(id).build()
        );
    }

    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public void delete(
            @PathVariable("id") final long id
    ) {
        service.deleteById(id);
    }
}

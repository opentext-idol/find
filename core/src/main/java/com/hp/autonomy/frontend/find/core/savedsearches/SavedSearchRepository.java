/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Set;

@NoRepositoryBean
public interface SavedSearchRepository<S extends SavedSearch<S>> extends CrudRepository<S, Long> {

    Set<S> findByActiveTrueAndUser_UserId(Long userId);

}

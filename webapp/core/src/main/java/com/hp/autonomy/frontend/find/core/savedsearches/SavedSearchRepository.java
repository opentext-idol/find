/*
 * Copyright 2016 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Set;

@NoRepositoryBean
public interface SavedSearchRepository<S extends SavedSearch<S, B>, B extends SavedSearch.Builder<S, B>> extends CrudRepository<S, Long> {

    Set<S> findByActiveTrueAndUser_UserId(Long userId);

    Set<S> findByUser_UserId(Long userId);

    S findByActiveTrueAndIdAndUser_UserId(Long id, Long userId);

    S findByActiveTrueAndId(Long id);
}

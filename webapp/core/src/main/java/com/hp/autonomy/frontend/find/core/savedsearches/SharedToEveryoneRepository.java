/*
 * Copyright 2017 Open Text.
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

import java.util.Set;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface SharedToEveryoneRepository extends CrudRepository<SharedToEveryone, SharedToEveryonePK> {

    @Query("SELECT r FROM com.hp.autonomy.frontend.find.core.savedsearches.SharedToEveryone r JOIN r.savedSearch s " +
            "WHERE s.active = true AND TYPE(s) = :type")
    Set<SharedToEveryone> findActiveByType(@Param("type") Class<?> type);

    SharedToEveryone findOneBySavedSearch_Id(Long searchId);
}

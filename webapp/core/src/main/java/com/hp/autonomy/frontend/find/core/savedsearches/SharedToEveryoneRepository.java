/*
 * Copyright 2017 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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

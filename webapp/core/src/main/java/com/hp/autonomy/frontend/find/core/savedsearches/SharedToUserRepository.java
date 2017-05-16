/*
 * Copyright 2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface SharedToUserRepository extends CrudRepository<SharedToUser, SharedToUserPK> {

    @Query("SELECT r FROM com.hp.autonomy.frontend.find.core.savedsearches.SharedToUser r JOIN r.savedSearch s " +
            "WHERE r.id.userId = :userId AND s.active = true AND TYPE(s) = :type")
    Set<SharedToUser> findByUserId(@Param("userId") Long userId, @Param("type") Class<?> type);

    Set<SharedToUser> findBySavedSearch_Id(Long searchId);
}

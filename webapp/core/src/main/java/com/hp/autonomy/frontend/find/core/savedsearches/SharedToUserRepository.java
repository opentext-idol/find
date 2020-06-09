/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.core.savedsearches;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface SharedToUserRepository extends CrudRepository<SharedToUser, SharedToUserPK> {

    @Query("SELECT r FROM com.hp.autonomy.frontend.find.core.savedsearches.SharedToUser r JOIN r.savedSearch s " +
            "WHERE r.id.userId = :userId AND s.active = true AND TYPE(s) = :type")
    Set<SharedToUser> findByUserId(@Param("userId") Long userId, @Param("type") Class<?> type);

    @Query("SELECT r FROM com.hp.autonomy.frontend.find.core.savedsearches.SharedToUser r JOIN r.savedSearch s " +
            "JOIN r.user u WHERE u.username LIKE :username AND s.id = :searchId AND s.active = true")
    Set<SharedToUser> findByUsernameAndSearchId(@Param("username") String username, @Param("searchId") Long searchId);

    Set<SharedToUser> findBySavedSearch_Id(Long searchId);
}

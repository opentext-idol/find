/*
 * Copyright 2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface SharedToUserRepository extends CrudRepository<SharedToUser, SharedToUserPK> {

    Set<SharedToUser> findByUser_UserIdAndSavedSearch_ActiveTrue(Long userId);

    Set<SharedToUser> findBySavedSearch_Id(Long searchId);
}

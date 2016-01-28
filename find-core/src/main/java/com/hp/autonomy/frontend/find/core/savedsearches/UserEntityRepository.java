/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

public interface UserEntityRepository extends CrudRepository<UserEntity, Long> {

    // Propagation set to REQUIRES_NEW in order to run this query in a new transaction, thereby avoiding an
    // infinite loop caused by spring JPA auditing (and the fact we run this query from the AuditAware.getCurrentAuditor() method).

    // See: http://forum.spring.io/forum/spring-projects/data/106312-spring-data-jpa-infinite-loop-when-updating-but-not-saving-an-auditable-object
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    UserEntity findByDomainAndUserStoreAndUuidAndUid(String domain, String userStore, UUID uuid, Long uid);
}

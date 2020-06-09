/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
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

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface UserEntityRepository extends CrudRepository<UserEntity, Long> {

    // Propagation set to REQUIRES_NEW in order to run this query in a new transaction, thereby avoiding an
    // infinite loop caused by spring JPA auditing (and the fact we run this query from the AuditAware.getCurrentAuditor() method).

    // See: http://forum.spring.io/forum/spring-projects/data/106312-spring-data-jpa-infinite-loop-when-updating-but-not-saving-an-auditable-object
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    UserEntity findByUsername(String username);
}

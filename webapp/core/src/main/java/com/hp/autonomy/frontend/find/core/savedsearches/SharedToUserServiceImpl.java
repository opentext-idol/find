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

import com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnExpression(BiConfiguration.BI_PROPERTY_SPEL)
public class SharedToUserServiceImpl implements SharedToUserService {
    private final SharedToUserRepository sharedToUserRepository;

    @Autowired
    public SharedToUserServiceImpl(final SharedToUserRepository sharedToUserRepository) {
        this.sharedToUserRepository = sharedToUserRepository;
    }

    @Override
    public SharedToUser save(final SharedToUser sharedToUser) {
        // merge in the created date if it exists
        final SharedToUser existingSharedToUser = sharedToUserRepository.findOne(sharedToUser.getId());
        sharedToUser.merge(existingSharedToUser);

        return sharedToUserRepository.save(sharedToUser);
    }
}

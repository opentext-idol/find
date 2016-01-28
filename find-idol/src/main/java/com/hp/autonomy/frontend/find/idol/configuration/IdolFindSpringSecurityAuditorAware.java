/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;


import com.hp.autonomy.frontend.configuration.authentication.CommunityPrincipal;
import com.hp.autonomy.frontend.find.core.configuration.AbstractFindSpringSecurityAuditorAware;
import com.hp.autonomy.frontend.find.core.savedsearches.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class IdolFindSpringSecurityAuditorAware extends AbstractFindSpringSecurityAuditorAware {

    @Override
    protected UserEntity principalToUser(Object principal) {
        final CommunityPrincipal communityPrincipal = (CommunityPrincipal) principal;

        final UserEntity userEntity = new UserEntity();
        userEntity.setUid(communityPrincipal.getId());

        return userEntity;
    }
}

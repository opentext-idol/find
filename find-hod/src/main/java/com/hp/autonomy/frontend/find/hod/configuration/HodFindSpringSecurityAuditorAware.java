/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.configuration;


import com.hp.autonomy.frontend.find.core.configuration.AbstractFindSpringSecurityAuditorAware;
import com.hp.autonomy.frontend.find.core.savedsearches.UserEntity;
import com.hp.autonomy.hod.sso.HodAuthenticationPrincipal;
import org.springframework.stereotype.Component;

@Component
public class HodFindSpringSecurityAuditorAware extends AbstractFindSpringSecurityAuditorAware {

    @Override
    protected UserEntity principalToUser(Object principal) {
        final HodAuthenticationPrincipal authenticationPrincipal = (HodAuthenticationPrincipal) principal;

        final UserEntity userEntity = new UserEntity();
        userEntity.setUuid(authenticationPrincipal.getUserUuid());
        userEntity.setUserStore(authenticationPrincipal.getUserStoreInformation().getUuid().toString());
        userEntity.setDomain(authenticationPrincipal.getUserStoreInformation().getDomain());

        return userEntity;
    }
}

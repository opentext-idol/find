/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.configuration;

import com.hp.autonomy.frontend.find.core.configuration.AbstractFindSpringSecurityAuditorAware;
import com.hp.autonomy.frontend.find.core.savedsearches.UserEntity;
import com.hp.autonomy.frontend.find.core.savedsearches.UserEntityRepository;
import com.hp.autonomy.hod.sso.HodAuthenticationPrincipal;
import com.hp.autonomy.searchcomponents.core.authentication.AuthenticationInformationRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HodFindSpringSecurityAuditorAware extends AbstractFindSpringSecurityAuditorAware<HodAuthenticationPrincipal> {
    @Autowired
    public HodFindSpringSecurityAuditorAware(
            final AuthenticationInformationRetriever<HodAuthenticationPrincipal> authenticationInformationRetriever,
            final UserEntityRepository userRepository
    ) {
        super(authenticationInformationRetriever, userRepository);
    }

    @Override
    protected UserEntity principalToUser(final HodAuthenticationPrincipal principal) {
        final UserEntity userEntity = new UserEntity();
        userEntity.setUuid(principal.getUserUuid());
        userEntity.setUserStore(principal.getUserStoreInformation().getUuid().toString());
        userEntity.setDomain(principal.getUserStoreInformation().getDomain());

        return userEntity;
    }
}

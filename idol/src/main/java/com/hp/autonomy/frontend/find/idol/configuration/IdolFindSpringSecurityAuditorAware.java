/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;


import com.hp.autonomy.frontend.configuration.authentication.CommunityPrincipal;
import com.hp.autonomy.frontend.find.core.configuration.AbstractFindSpringSecurityAuditorAware;
import com.hp.autonomy.frontend.find.core.savedsearches.UserEntity;
import com.hp.autonomy.frontend.find.core.savedsearches.UserEntityRepository;
import com.hp.autonomy.searchcomponents.core.authentication.AuthenticationInformationRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty("hp.find.enableBi")
public class IdolFindSpringSecurityAuditorAware extends AbstractFindSpringSecurityAuditorAware<CommunityPrincipal> {
    @Autowired
    public IdolFindSpringSecurityAuditorAware(
            final AuthenticationInformationRetriever<?, CommunityPrincipal> authenticationInformationRetriever,
            final UserEntityRepository userRepository
    ) {
        super(authenticationInformationRetriever, userRepository);
    }

    @Override
    protected UserEntity principalToUser(final CommunityPrincipal principal) {
        final UserEntity userEntity = new UserEntity();
        userEntity.setUid(principal.getId());

        return userEntity;
    }
}

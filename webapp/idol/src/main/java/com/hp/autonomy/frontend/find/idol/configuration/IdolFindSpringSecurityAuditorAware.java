/*
 * Copyright 2018 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.idol.configuration;


import com.hp.autonomy.frontend.configuration.authentication.CommunityPrincipal;
import com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration;
import com.hp.autonomy.frontend.find.core.configuration.AbstractFindSpringSecurityAuditorAware;
import com.hp.autonomy.frontend.find.core.savedsearches.UserEntity;
import com.hp.autonomy.frontend.find.core.savedsearches.UserEntityRepository;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnExpression(BiConfiguration.BI_PROPERTY_SPEL)
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
        userEntity.setUsername(principal.getUsername());

        return userEntity;
    }
}

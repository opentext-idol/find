/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.configuration.authentication.OneToOneOrZeroSimpleAuthorityMapper;
import com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration;
import com.hp.autonomy.frontend.find.core.beanconfiguration.FindRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;

@Configuration
public class UserConfiguration {
    public static final String IDOL_USER_ROLE = "FindUser";
    public static final String IDOL_ADMIN_ROLE = "FindAdmin";
    public static final String IDOL_BI_ROLE = "FindBI";

    @Value("${" + BiConfiguration.BI_PROPERTY + '}')
    private boolean enableBi;

    @Bean
    public GrantedAuthoritiesMapper grantedAuthoritiesMapper() {
        final ImmutableMap.Builder<String, String> rolesMapBuilder = ImmutableMap.<String, String>builder()
            .put(IDOL_USER_ROLE, FindRole.USER.toString())
            .put(IDOL_ADMIN_ROLE, FindRole.ADMIN.toString());

        if (enableBi) {
            rolesMapBuilder.put(IDOL_BI_ROLE, FindRole.BI.toString());
        }

        return new OneToOneOrZeroSimpleAuthorityMapper(rolesMapBuilder.build());
    }
}

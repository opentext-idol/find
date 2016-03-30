/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.configuration.authentication.OneToOneOrZeroSimpleAuthorityMapper;
import com.hp.autonomy.frontend.find.core.beanconfiguration.FindRole;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;

import java.util.Map;

@Configuration
public class UserConfiguration {
    public static final String IDOL_USER_ROLE = "FindUser";
    public static final String IDOL_ADMIN_ROLE = "FindAdmin";

    @Bean
    public GrantedAuthoritiesMapper grantedAuthoritiesMapper() {
        final Map<String, String> rolesMap = ImmutableMap.<String, String>builder()
            .put(IDOL_USER_ROLE, FindRole.USER.toString())
            .put(IDOL_ADMIN_ROLE, FindRole.ADMIN.toString())
            .build();

        return new OneToOneOrZeroSimpleAuthorityMapper(rolesMap);
    }
}

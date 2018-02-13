/*
 * Copyright 2014-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import com.hp.autonomy.frontend.configuration.authentication.OneToOneOrZeroSimpleAuthorityMapper;
import com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration;
import com.hp.autonomy.frontend.find.core.beanconfiguration.FindRole;
import java.util.Collections;
import java.util.Map;

import com.hp.autonomy.frontend.find.idol.authentication.FindCommunityRole;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;

@Configuration
public class UserConfiguration {
    @Value(BiConfiguration.BI_PROPERTY_SPEL)
    private boolean enableBi;

    @Bean
    public GrantedAuthoritiesMapper grantedAuthoritiesMapper() {
        final Map<String, String> rolesMap = new CaseInsensitiveMap<>();

        rolesMap.put(FindCommunityRole.USER.value(), FindRole.USER.toString());
        rolesMap.put(FindCommunityRole.ADMIN.value(), FindRole.ADMIN.toString());

        if (enableBi) {
            rolesMap.put(FindCommunityRole.BI.value(), FindRole.BI.toString());
        }

        return new OneToOneOrZeroSimpleAuthorityMapper(Collections.unmodifiableMap(rolesMap));
    }
}

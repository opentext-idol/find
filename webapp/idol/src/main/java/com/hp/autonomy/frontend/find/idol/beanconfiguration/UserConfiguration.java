/*
 * Copyright 2014-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import com.hp.autonomy.frontend.configuration.authentication.OneToOneOrZeroSimpleAuthorityMapper;
import com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration;
import com.hp.autonomy.frontend.find.core.beanconfiguration.FindRole;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.hp.autonomy.frontend.find.idol.authentication.FindCommunityRole;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;

import javax.annotation.PostConstruct;

@Configuration
public class UserConfiguration {
    @Value(BiConfiguration.BI_PROPERTY_SPEL)
    private boolean enableBi;
    private final BidiMap<String, String> communityInternalRoleMap;

    public UserConfiguration() {
        communityInternalRoleMap = new DualHashBidiMap<>();
    }

    @PostConstruct
    private void init() {
        communityInternalRoleMap.put(FindCommunityRole.USER.value(), FindRole.USER.toString());
        communityInternalRoleMap.put(FindCommunityRole.ADMIN.value(), FindRole.ADMIN.toString());
        if (enableBi) {
            communityInternalRoleMap.put(FindCommunityRole.BI.value(), FindRole.BI.toString());
        }
    }

    public String getInternalRole(final String communityRole) {
        return communityInternalRoleMap.get(communityRole);
    }

    public String getCommunityRole(final String internalRole) {
        return communityInternalRoleMap.inverseBidiMap().get(internalRole);
    }

    public List<String> getCommunityRoles(final AuthenticationInformationRetriever<?, ?> authInfoRetriever) {
        return authInfoRetriever.getAuthentication().getAuthorities().stream()
            .map(authority -> getCommunityRole(authority.getAuthority()))
            .filter(role -> role != null)
            .collect(Collectors.toList());
    }

    @Bean
    public GrantedAuthoritiesMapper grantedAuthoritiesMapper() {
        return new OneToOneOrZeroSimpleAuthorityMapper(
            Collections.unmodifiableMap(new CaseInsensitiveMap<>(communityInternalRoleMap)));
    }
}

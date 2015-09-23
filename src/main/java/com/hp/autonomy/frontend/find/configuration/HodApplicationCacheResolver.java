/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.configuration;

import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.sso.HodAuthentication;
import org.springframework.cache.interceptor.AbstractCacheResolver;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

// TODO if this works we need to librarify it
public class HodApplicationCacheResolver extends AbstractCacheResolver {
    static final String SEPARATOR = ":";

    @Override
    protected Collection<String> getCacheNames(final CacheOperationInvocationContext<?> context) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof HodAuthentication)) {
            throw new IllegalStateException("There is no HOD authentication token in the security context holder");
        }

        final HodAuthentication hodAuthentication = (HodAuthentication) authentication;
        final String applicationId = new ResourceIdentifier(hodAuthentication.getDomain(), hodAuthentication.getApplication()).toString();

        final Set<String> contextCacheNames = context.getOperation().getCacheNames();
        final Set<String> resolvedCacheNames = new HashSet<>();

        for (final String cacheName : contextCacheNames) {
            resolvedCacheNames.add(applicationId + SEPARATOR + cacheName);
        }

        return resolvedCacheNames;
    }

    public static String getOriginalName(final String resolvedName) {
        final String[] cacheNameComponents = resolvedName.split(SEPARATOR);
        return cacheNameComponents[cacheNameComponents.length - 1];
    }
}

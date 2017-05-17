/*
 * Copyright 2014-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.springframework.security.core.GrantedAuthority;

class GrantedAuthorityMatcher extends TypeSafeMatcher<GrantedAuthority> {

    private final String authority;

    private GrantedAuthorityMatcher(final String authority) {
        super();

        this.authority = authority;
    }

    static GrantedAuthorityMatcher authority(final String authority) {
        return new GrantedAuthorityMatcher(authority);
    }

    @Override
    protected boolean matchesSafely(final GrantedAuthority item) {
        return item.getAuthority().equals(authority);
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText(authority);
    }
}

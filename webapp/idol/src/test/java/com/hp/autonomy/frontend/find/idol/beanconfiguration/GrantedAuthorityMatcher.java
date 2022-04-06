/*
 * (c) Copyright 2014-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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

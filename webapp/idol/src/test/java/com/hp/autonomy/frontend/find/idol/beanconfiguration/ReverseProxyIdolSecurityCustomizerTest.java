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

import com.hp.autonomy.user.UserRoles;
import com.hp.autonomy.user.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;

import static com.hp.autonomy.frontend.find.idol.beanconfiguration.ReverseProxyIdolSecurityCustomizer.PRE_AUTHENTICATED_ROLES_PROPERTY_KEY;
import static com.hp.autonomy.frontend.find.idol.beanconfiguration.ReverseProxyIdolSecurityCustomizer.PRE_AUTHENTICATED_USERNAME_PROPERTY_KEY;
import static com.hp.autonomy.frontend.find.idol.beanconfiguration.ReverseProxyIdolSecurityCustomizer.REVERSE_PROXY_PROPERTY_KEY;
import static com.hp.autonomy.frontend.find.idol.beanconfiguration.GrantedAuthorityMatcher.authority;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = ReverseProxyIdolSecurityCustomizer.class,
        properties = {
                REVERSE_PROXY_PROPERTY_KEY + "=true",
                PRE_AUTHENTICATED_ROLES_PROPERTY_KEY + "=FindUser,FindBI,FindAdmin",
                PRE_AUTHENTICATED_USERNAME_PROPERTY_KEY + "="
        },
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ReverseProxyIdolSecurityCustomizerTest {

    @Autowired
    private ReverseProxyIdolSecurityCustomizer reverseProxyIdolSecurityCustomizer;

    @MockBean
    private UserService userService;

    @MockBean
    private GrantedAuthoritiesMapper authoritiesMapper;

    @Mock
    private Authentication foreignAuthentication;

    @Mock
    private UserRoles userRoles;

    @Before
    public void setUp() {
        when(foreignAuthentication.getPrincipal()).thenReturn("Some Guy");
        when(foreignAuthentication.getCredentials()).thenReturn("password");
        when(foreignAuthentication.isAuthenticated()).thenReturn(false);

        // given we're returning the first argument anything with a bad type shouldn't compile
        //noinspection unchecked
        when(authoritiesMapper.mapAuthorities(anyCollection())).then(returnsFirstArg());

        when(userService.getUser(anyString(), anyBoolean())).thenReturn(userRoles);
    }

    // We don't need to filter the roles anymore, so we just test the mapping.
    @Test
    public void testRoleFiltering() {
        final Collection<AuthenticationProvider> authenticationProviders = reverseProxyIdolSecurityCustomizer.getAuthenticationProviders();

        assertThat(authenticationProviders, hasSize(1));

        final Authentication authentication = authenticationProviders.stream()
                .map(authenticationProvider -> authenticationProvider.authenticate(this.foreignAuthentication))
                .findFirst()
                .orElseThrow(() -> new AssertionError("AuthenticationProvider did not authenticate"));

        assertThat(authentication.getAuthorities(), contains(authority("FindAdmin"), authority("FindUser"), authority("FindBI")));
    }


}

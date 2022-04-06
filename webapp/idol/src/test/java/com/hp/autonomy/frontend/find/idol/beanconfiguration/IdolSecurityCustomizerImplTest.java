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

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.authentication.CommunityAuthentication;
import com.hp.autonomy.frontend.find.core.savedsearches.UserEntityRepository;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.user.UserRoles;
import com.hp.autonomy.user.UserService;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;

import static com.hp.autonomy.frontend.find.idol.beanconfiguration.GrantedAuthorityMatcher.authority;
import static com.hp.autonomy.frontend.find.idol.beanconfiguration.IdolSecurityCustomizerImpl.DEFAULT_ROLES_KEY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = IdolSecurityCustomizerImpl.class,
        properties = {
                DEFAULT_ROLES_KEY + "=FindUser"
        },
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class IdolSecurityCustomizerImplTest {

    @Autowired
    private IdolSecurityCustomizerImpl idolSecurityCustomizer;

    @MockBean
    private ConfigService<IdolFindConfig> configService;

    @MockBean
    private UserService userService;

    @MockBean
    private GrantedAuthoritiesMapper authoritiesMapper;

    // required for wiring but not used in test
    @SuppressWarnings("unused")
    @MockBean
    private AuthenticationInformationRetriever<?, ?> authenticationInformationRetriever;

    @Mock
    private Authentication foreignAuthentication;

    @Mock
    private CommunityAuthentication communityAuthentication;

    @Mock
    private IdolFindConfig idolFindConfig;

    @Before
    public void setUp() {
        when(foreignAuthentication.getPrincipal()).thenReturn("Some Guy");
        when(foreignAuthentication.getCredentials()).thenReturn("password");
        when(foreignAuthentication.isAuthenticated()).thenReturn(false);

        // given we're returning the first argument anything with a bad type shouldn't compile
        //noinspection unchecked
        when(authoritiesMapper.mapAuthorities(anyCollection())).then(returnsFirstArg());

        when(userService.authenticateUser(anyString(), anyString(), anyString())).thenReturn(true);
        when(userService.getUser(anyString(), anyBoolean(), anyString())).thenReturn(new UserRoles("Some Guy"));

        when(communityAuthentication.getMethod()).thenReturn("LDAP");

        // use the not type safe syntax as the usual version won't compile
        Mockito.doReturn(communityAuthentication).when(idolFindConfig).getAuthentication();
        when(configService.getConfig()).thenReturn(idolFindConfig);
    }

    @Test
    public void testDefaultRoles() {
        final Collection<AuthenticationProvider> authenticationProviders = idolSecurityCustomizer.getAuthenticationProviders();

        assertThat(authenticationProviders, hasSize(1));

        final Authentication authentication = authenticationProviders.stream()
                .findFirst()
                .map(authenticationProvider -> authenticationProvider.authenticate(this.foreignAuthentication))
                .orElseThrow(() -> new AssertionError("AuthenticationProvider did not authenticate"));

        assertThat(authentication.getAuthorities(), contains(authority("FindUser")));
    }

}

package com.hp.autonomy.frontend.find.idol.authentication;

import com.autonomy.aci.client.services.AciErrorException;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import static com.hp.autonomy.frontend.find.idol.authentication.IdolPreAuthenticatedAuthenticationProvider.USER_NOT_FOUND_ERROR_ID;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = IdolPreAuthenticatedAuthenticationProvider.class, properties = "server.reverseProxy=true", webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class IdolPreAuthenticatedAuthenticationProviderTest {
    private static final String SAMPLE_USER = "some_user";

    @MockBean
    private UserService userService;
    @MockBean
    private GrantedAuthoritiesMapper authoritiesMapper;
    @Autowired
    private AuthenticationProvider authenticationProvider;
    @Mock
    private Authentication authentication;
    @Mock
    private Principal principal;

    public IdolPreAuthenticatedAuthenticationProviderTest() {
    }

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        when(authentication.getPrincipal()).thenReturn(principal);
        when(principal.toString()).thenReturn(SAMPLE_USER);
        when(authoritiesMapper.mapAuthorities(any())).thenAnswer(invocation -> ((Collection<? extends GrantedAuthority>) invocation.getArgumentAt(0, Collection.class))
                .stream()
                .map(x -> mock(GrantedAuthority.class))
                .collect(Collectors.toList()));
    }

    @Test
    public void authenticateWithExistingUser() {
        when(userService.getUser(SAMPLE_USER)).thenReturn(new UserRoles(SAMPLE_USER, 123L, null, Collections.singletonList(FindCommunityRole.USER.value())));
        final Authentication communityAuthentication = authenticationProvider.authenticate(authentication);
        assertTrue(communityAuthentication.isAuthenticated());
        assertThat(communityAuthentication.getAuthorities(), hasSize(1));
    }

    @Test
    public void authenticateWithNewUser() {
        final AciErrorException aciErrorException = new AciErrorException();
        aciErrorException.setErrorId(USER_NOT_FOUND_ERROR_ID);
        when(userService.getUser(SAMPLE_USER))
                .thenThrow(aciErrorException)
                .thenReturn(new UserRoles(SAMPLE_USER, 123L, null, Collections.singletonList(FindCommunityRole.USER.value())));
        final Authentication communityAuthentication = authenticationProvider.authenticate(authentication);
        assertTrue(communityAuthentication.isAuthenticated());
        assertThat(communityAuthentication.getAuthorities(), hasSize(1));
    }

    @Test(expected = BadCredentialsException.class)
    public void authenticateWithNoPrincipal() {
        when(authentication.getPrincipal()).thenReturn(null);
        authenticationProvider.authenticate(authentication);
    }

    @Test(expected = AciErrorException.class)
    public void communityError() {
        when(userService.getUser(SAMPLE_USER)).thenThrow(new AciErrorException());
        authenticationProvider.authenticate(authentication);
    }

    @Test
    public void supports() {
        assertTrue(authenticationProvider.supports(PreAuthenticatedAuthenticationToken.class));
        assertFalse(authenticationProvider.supports(UsernamePasswordAuthenticationToken.class));
    }
}

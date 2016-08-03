package com.hp.autonomy.frontend.find.hod.authentication;

import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.hod.sso.HodUsernameResolver;
import org.junit.Test;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class HsodUsernameResolverTest {
    @Test
    public void usesCorrectDisplayName() {
        final Map<String, Serializable> metadata = ImmutableMap.of(HavenSearchUserMetadata.USER_DISPLAY_NAME, "James Bond");

        final HodUsernameResolver hsodUsernameResolver = new HsodUsernameResolver();

        final String displayName = hsodUsernameResolver.resolve(metadata);
        assertEquals("James Bond", displayName);
    }

    @Test
    public void usesCorrectLegacyDisplayName() {
        final Map<String, Serializable> metadata = ImmutableMap.of(HavenSearchUserMetadata.LEGACY_USER_DISPLAY_NAME, "Jimmy Bond");

        final HodUsernameResolver hsodUsernameResolver = new HsodUsernameResolver();

        final String displayName = hsodUsernameResolver.resolve(metadata);
        assertEquals("Jimmy Bond", displayName);
    }

    @Test
    public void usesCorrectDisplayNameOverLegacyDisplayName() {
        final Map<String, Serializable> metadata = ImmutableMap.of(
                HavenSearchUserMetadata.USER_DISPLAY_NAME, "James Bond",
                HavenSearchUserMetadata.LEGACY_USER_DISPLAY_NAME, "Jimmy Bond");

        final HodUsernameResolver hsodUsernameResolver = new HsodUsernameResolver();

        final String displayName = hsodUsernameResolver.resolve(metadata);
        assertEquals("James Bond", displayName);
    }

    @Test
    public void noUserInformation() {
        final HodUsernameResolver hsodUsernameResolver = new HsodUsernameResolver();

        final String displayName = hsodUsernameResolver.resolve(Collections.emptyMap());
        assertNull(displayName);
    }

    @Test
    public void nonStringUserInformation() {
        final Map<String, Serializable> metadata = ImmutableMap.of(HavenSearchUserMetadata.USER_DISPLAY_NAME, mock(Serializable.class));

        final HodUsernameResolver hsodUsernameResolver = new HsodUsernameResolver();

        final String displayName = hsodUsernameResolver.resolve(metadata);
        assertNull(displayName);
    }
}

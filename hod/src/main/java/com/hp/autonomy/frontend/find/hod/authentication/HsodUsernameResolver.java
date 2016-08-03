/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.authentication;

import com.hp.autonomy.hod.sso.HodUsernameResolver;

import java.io.Serializable;
import java.util.Map;

public class HsodUsernameResolver implements HodUsernameResolver {
    @Override
    public String resolve(final Map<String, Serializable> metadata) {
        final Serializable serializableName = metadata.get(metadata.get(HavenSearchUserMetadata.USER_DISPLAY_NAME) != null
                ? HavenSearchUserMetadata.USER_DISPLAY_NAME
                : HavenSearchUserMetadata.LEGACY_USER_DISPLAY_NAME);

        return serializableName instanceof String ? (String) serializableName : null;
    }
}

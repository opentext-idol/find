package com.hp.autonomy.frontend.find.hod.authentication;

import com.hp.autonomy.hod.sso.HodUsernameResolver;

import java.io.Serializable;
import java.util.Map;

public class HsodUsernameResolver implements HodUsernameResolver {
    @Override
    public String resolve(final Map<String, Serializable> metadata) {
        final Serializable serializableName = metadata.get(HavenSearchUserMetadata.USERNAME);
        return serializableName instanceof String ? (String) serializableName : null;
    }
}

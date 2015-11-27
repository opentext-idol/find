package com.hp.autonomy.frontend.find.hod.parametricfields;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.parametricfields.ParametricRequestBuilder;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.parametricvalues.HodParametricRequest;
import com.hp.autonomy.hod.sso.HodAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class HodParametricRequestBuilder implements ParametricRequestBuilder<HodParametricRequest, ResourceIdentifier> {
    @Autowired
    private ConfigService<HodFindConfig> configService;

    @Override
    public HodParametricRequest buildRequest(final Set<ResourceIdentifier> databases, final Set<String> fieldNames, final String queryText, final String fieldText) {
        final String profileName = configService.getConfig().getQueryManipulation().getProfile();
        final String domain = ((HodAuthentication) SecurityContextHolder.getContext().getAuthentication()).getPrincipal().getApplication().getDomain();

        return new HodParametricRequest.Builder()
                .setQueryProfile(new ResourceIdentifier(domain, profileName))
                .setDatabases(databases)
                .setFieldNames(fieldNames)
                .setQueryText(queryText)
                .setFieldText(fieldText)
                .build();
    }
}

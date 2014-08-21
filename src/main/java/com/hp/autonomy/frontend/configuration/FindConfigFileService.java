package com.hp.autonomy.frontend.configuration;

import com.autonomy.frontend.configuration.AbstractAuthenticatingConfigFileService;
import com.autonomy.frontend.configuration.Authentication;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FindConfigFileService extends AbstractAuthenticatingConfigFileService<FindConfig> {

    @Override
    protected void addPreReadMixins(final ObjectMapper mapper) {
        mapper.addMixInAnnotations(Authentication.class, AuthenticationMixins.class);
    }

    @Override
    public FindConfig preUpdate(final FindConfig config) {
        return config;
    }

    @Override
    public void postUpdate(final FindConfig config) throws Exception {

    }

    @Override
    public void postInitialise(final FindConfig config) throws Exception {
        postUpdate(config);
    }

    @Override
    public Class<FindConfig> getConfigClass() {
        return FindConfig.class;
    }

    @Override
    public FindConfig getEmptyConfig() {
        return new FindConfig.Builder().build();
    }

}

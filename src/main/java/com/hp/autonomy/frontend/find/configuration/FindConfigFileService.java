package com.hp.autonomy.frontend.find.configuration;

import com.hp.autonomy.frontend.configuration.AbstractAuthenticatingConfigFileService;

public class FindConfigFileService extends AbstractAuthenticatingConfigFileService<FindConfig> {

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

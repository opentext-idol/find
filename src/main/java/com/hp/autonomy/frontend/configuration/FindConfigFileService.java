package com.hp.autonomy.frontend.configuration;

import com.autonomy.frontend.configuration.AbstractConfigFileService;

public class FindConfigFileService extends AbstractConfigFileService<FindConfig> {

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

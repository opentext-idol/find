/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.hp.autonomy.frontend.configuration.ConfigurableAciService;

/**
 * AciService which need not be configured
 */
public interface OptionalAciService extends ConfigurableAciService {
    /**
     * Whether or not the server corresponding to this configuration is enabled
     *
     * @return whether or not the server corresponding to this configuration is enabled
     */
    boolean isEnabled();
}

/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.hp.autonomy.frontend.configuration.ConfigException;

@SuppressWarnings("WeakerAccess")
public interface ConfigurationComponent<C extends ConfigurationComponent<C>> {
    C merge(C other);

    void basicValidate(String... options) throws ConfigException;
}

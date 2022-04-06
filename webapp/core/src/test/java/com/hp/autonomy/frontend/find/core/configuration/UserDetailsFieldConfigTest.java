/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.configuration.ConfigException;
import org.junit.Test;

import java.util.Collections;

public class UserDetailsFieldConfigTest {

    @Test
    public void testBasicValidate_valid() throws ConfigException {
        final UserDetailsFieldConfig config = UserDetailsFieldConfig.builder()
            .name("the field")
            .build();
        config.basicValidate("details");
    }

    @Test(expected = ConfigException.class)
    public void testBasicValidate_nameMissing() throws ConfigException {
        final UserDetailsFieldConfig config = UserDetailsFieldConfig.builder().build();
        config.basicValidate("details");
    }

}

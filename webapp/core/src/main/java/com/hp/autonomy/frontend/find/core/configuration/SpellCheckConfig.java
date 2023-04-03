/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import com.hp.autonomy.frontend.configuration.validation.OptionalConfigurationComponent;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
@JsonDeserialize(builder = SpellCheckConfig.SpellCheckConfigBuilder.class)
public class SpellCheckConfig extends SimpleComponent<SpellCheckConfig>
    implements OptionalConfigurationComponent<SpellCheckConfig>
{
    /**
     * Whether spellcheck as a whole is enabled.
     */
    private final Boolean enabled;

    @JsonPOJOBuilder(withPrefix = "")
    public static class SpellCheckConfigBuilder {
        private Boolean enabled = true;
    }

}

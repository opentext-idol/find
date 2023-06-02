/*
 * Copyright 2020 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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

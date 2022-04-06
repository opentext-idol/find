/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.users;

import com.hp.autonomy.types.idol.responses.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * User from a search result.
 */
@AllArgsConstructor
@Getter
public class RelatedUser {
    /**
     * User details.
     */
    private final User user;
    /**
     * Whether the user is explicitly tagged as a relevant 'expert', rather than just having an
     * interest in the topic.
     */
    private final boolean isExpert;
}

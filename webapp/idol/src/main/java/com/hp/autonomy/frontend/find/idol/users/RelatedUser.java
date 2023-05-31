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

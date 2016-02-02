/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.web;

import org.springframework.security.core.Authentication;

public interface AuthenticationInformationRetriever<A extends Authentication> {
    A getAuthentication();
}

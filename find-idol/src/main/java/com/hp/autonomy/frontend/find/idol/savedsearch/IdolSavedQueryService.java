/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.savedsearch;

import com.hp.autonomy.frontend.find.core.savedsearches.SavedQueryServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class IdolSavedQueryService extends SavedQueryServiceImpl<String> {
    public IdolSavedQueryService() {
        super("Wookieepedia");
    }
}

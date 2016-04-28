/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.typeahead;

import com.hp.autonomy.frontend.find.HodFindApplication;
import com.hp.autonomy.frontend.find.core.typeahead.AbstractTypeAheadIT;
import org.springframework.boot.test.SpringApplicationConfiguration;

@SpringApplicationConfiguration(classes = HodFindApplication.class)
public class TypeAheadIT extends AbstractTypeAheadIT {
    public TypeAheadIT() {
        super("fleetwo", "fleetwood mac");
    }
}

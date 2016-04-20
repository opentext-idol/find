/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.search;

import com.hp.autonomy.frontend.find.HodFindApplication;
import com.hp.autonomy.frontend.find.core.search.AbstractDocumentServiceIT;
import com.hp.autonomy.searchcomponents.hod.test.HodTestConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;

@SpringApplicationConfiguration(classes = {HodTestConfiguration.class, HodFindApplication.class})
public class HodDocumentServiceIT extends AbstractDocumentServiceIT {
}

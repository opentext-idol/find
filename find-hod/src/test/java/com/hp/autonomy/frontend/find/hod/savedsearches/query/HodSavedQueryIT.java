/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.savedsearches.query;

import com.hp.autonomy.frontend.find.HodFindApplication;
import com.hp.autonomy.frontend.find.core.savedsearches.query.AbstractSavedQueryIT;
import com.hp.autonomy.searchcomponents.hod.test.HodTestConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;

@SpringApplicationConfiguration(classes = {HodTestConfiguration.class, HodFindApplication.class})
public class HodSavedQueryIT extends AbstractSavedQueryIT {}

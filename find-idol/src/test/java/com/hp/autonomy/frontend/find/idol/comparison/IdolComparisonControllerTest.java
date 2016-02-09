/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.comparison;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.comparison.ComparisonControllerTest;
import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IdolComparisonControllerTest extends ComparisonControllerTest<String, IdolSearchResult, AciErrorException> {
}

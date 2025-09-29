/*
 * Copyright 2017 Open Text.
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

package com.hp.autonomy.frontend.find.core.savedsearches.query;

import com.hp.autonomy.frontend.find.core.savedsearches.AbstractSavedSearchServiceTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = SavedQueryService.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class SavedQueryServiceTest extends AbstractSavedSearchServiceTest<SavedQuery, SavedQuery.Builder> {
    @SuppressWarnings("unused")
    @MockitoBean
    private SavedQueryRepository crudRepository;

    public SavedQueryServiceTest() {
        super(SavedQuery.Builder::new);
    }
}

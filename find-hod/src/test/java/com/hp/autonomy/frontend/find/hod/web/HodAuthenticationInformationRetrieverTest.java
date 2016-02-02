/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.web;

import com.hp.autonomy.frontend.find.core.web.AbstractAuthenticationInformationRetrieverTest;
import com.hp.autonomy.hod.sso.HodAuthentication;
import org.junit.Before;
import org.mockito.Mock;

public class HodAuthenticationInformationRetrieverTest extends AbstractAuthenticationInformationRetrieverTest<HodAuthentication> {
    @Mock
    private HodAuthentication hodAuthentication;

    @Before
    public void setUp() {
        authenticationInformationRetriever = new HodAuthenticationInformationRetriever();
        authentication = hodAuthentication;
    }
}

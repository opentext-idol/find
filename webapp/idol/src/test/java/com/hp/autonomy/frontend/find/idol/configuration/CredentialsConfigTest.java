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

package com.hp.autonomy.frontend.find.idol.configuration;

import org.jasypt.util.text.TextEncryptor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class CredentialsConfigTest {
    private static final CredentialsConfig decryptedValue = new CredentialsConfig("user", "pass");
    private TextEncryptor encryptor;

    @Before
    public void setUp() {
        encryptor = Mockito.mock(TextEncryptor.class);
        Mockito.when(encryptor.encrypt(Mockito.any()))
            .then(invocation -> "encrypted:" + invocation.getArgument(0, String.class));
        Mockito.when(encryptor.decrypt(Mockito.any()))
            .then(invocation -> invocation.getArgument(0, String.class).substring(10));
    }

    @Test
    public void testWithoutPasswords() {
        Assert.assertEquals(new CredentialsConfig("user", ""), decryptedValue.withoutPasswords());
    }

    @Test
    public void testWithEncryptedPasswords() {
        Assert.assertEquals(new CredentialsConfig("user", "encrypted:pass"),
            decryptedValue.withEncryptedPasswords(encryptor));
    }

    @Test
    public void testWithDecryptedPasswords() {
        Assert.assertEquals(new CredentialsConfig("user", "pass"),
            new CredentialsConfig("user", "encrypted:pass").withDecryptedPasswords(encryptor));
    }

    @Test
    public void testMerge_missingValues() {
        Assert.assertEquals(new CredentialsConfig("user", "pass"),
            new CredentialsConfig(null, null).merge(decryptedValue));
    }

    @Test
    public void testMerge_providedValues() {
        Assert.assertEquals(new CredentialsConfig("new user", "new pass"),
            new CredentialsConfig("new user", "new pass").merge(decryptedValue));
    }

    @Test
    public void testMerge_emptyPassword() {
        Assert.assertEquals(new CredentialsConfig("new user", "pass"),
            new CredentialsConfig("new user", "").merge(decryptedValue));
    }

}

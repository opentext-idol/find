/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import org.springframework.beans.factory.FactoryBean;

/**
 * Generates the password for encrypting the PostgresPassword
 */
public class TextEncryptorPasswordFactory implements FactoryBean<String> {

    // exception declared in external interface
    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Override
    public String getObject() throws Exception {
        // don't change this or existing config files with encrypted text will stop working
        return "sdfjnhejsRUHR$uwhr843y5432rjsadfjsehR$HWENFU5y472345792348yJGNEO";
    }

    @Override
    public Class<?> getObjectType() {
        return String.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}

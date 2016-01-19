/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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

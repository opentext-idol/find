/*
 * (c) Copyright 2014-2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.idol.web;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JavaxXmlSettings {

    private JavaxXmlSettings() {}

    public static void apply() {
        setPropertyToClass("javax.xml.transform.TransformerFactory", "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
        setPropertyToClass("javax.xml.parsers.DocumentBuilderFactory", "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
    }

    private static void setPropertyToClass(final String property, final String className) {
        // don't clobber settings set by users who know what they're doing
        if (System.getProperty(property) == null) {
            try {
                // check the class exists before we set the property
                Class.forName(className);
                
                System.setProperty(property, className);
            } catch (final ClassNotFoundException e) {
                log.warn("Class {} not found, using default value for {}", className, property);
            }
        }
    }
}

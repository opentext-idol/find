/*
 * Copyright 2018 Open Text.
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

package com.hp.autonomy.frontend.find.core.logging;

import java.io.CharArrayWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.valves.AbstractAccessLogValve;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@Slf4j
public class TomcatAccessLogValve extends AbstractAccessLogValve {

    private static final Marker TomcatAccess = MarkerFactory.getMarker("TomcatAccess");

    @Override
    protected void log(final CharArrayWriter message) {
        log.trace(TomcatAccess, message.toString());
    }
}

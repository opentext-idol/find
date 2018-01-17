/*
 * Copyright 2018, Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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

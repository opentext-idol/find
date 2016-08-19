/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.stats;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.hp.autonomy.frontend.find.core.stats.Event;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Generates the root element required for Stats Server
 */
@JacksonXmlRootElement(localName = "events")
@Data
@AllArgsConstructor
public class Events {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "find-event")
    private List<Event> events;

}

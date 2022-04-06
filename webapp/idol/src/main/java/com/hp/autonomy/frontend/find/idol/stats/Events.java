/*
 * (c) Copyright 2014-2016 Micro Focus or one of its affiliates.
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

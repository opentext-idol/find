/*
 * Copyright 2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.themetracker;

import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.services.impl.AbstractStAXProcessor;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

public class TermsProcessor extends AbstractStAXProcessor<List<String>> {

    @Override
    public List<String> process(final XMLStreamReader xmlStreamReader) {
        try {
            final List<String> terms = new ArrayList<String>();

            while(xmlStreamReader.hasNext()) {
                final int evtType = xmlStreamReader.next();
                switch (evtType) {
                    case XMLEvent.START_ELEMENT:
                        if ("autn:term".equals(xmlStreamReader.getLocalName())) {
                            terms.add(xmlStreamReader.getElementText());
                        }
                        break;
                    case XMLEvent.END_ELEMENT:
                        if ("autn:cluster".equals(xmlStreamReader.getLocalName())) {
                            return terms;
                        }
                }
            }

            throw new ProcessorException("Did not find a closing autn:cluster");
        } catch (XMLStreamException e) {
            throw new ProcessorException("Error reading XML", e);
        }
    }
}

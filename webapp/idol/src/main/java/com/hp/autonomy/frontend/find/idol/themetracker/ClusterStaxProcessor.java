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

public class ClusterStaxProcessor extends AbstractStAXProcessor<List<Cluster>> {

    private final String jobName;

    public ClusterStaxProcessor(final String jobName) {
        this.jobName = jobName;
    }

    @Override
    public List<Cluster> process(final XMLStreamReader xmlStreamReader) {
        try {
            final List<Cluster> clusters = new ArrayList<Cluster>();
            String title = null;
            long fromDate, toDate;
            int numDocs, x1, x2, y1, y2, id;
            fromDate = toDate = numDocs = x1 = x2 = y1 = y2 = id = 0;

            while(xmlStreamReader.hasNext()) {
                final int evtType = xmlStreamReader.next();
                switch (evtType) {
                    case XMLEvent.START_ELEMENT:
                        final String localName = xmlStreamReader.getLocalName();
                        if ("autn:cluster".equals(localName)) {
                            fromDate = toDate = numDocs = x1 = x2 = y1 = y2 = id = 0;
                            title = null;
                        }
                        else if ("autn:title".equals(localName)) {
                            title = xmlStreamReader.getElementText();
                        }
                        else if ("autn:fromdate".equals(localName)) {
                            fromDate = Long.parseLong(xmlStreamReader.getElementText());
                        }
                        else if ("autn:todate".equals(localName)) {
                            toDate = Long.parseLong(xmlStreamReader.getElementText());
                        }
                        else if ("autn:numdocs".equals(localName)) {
                            numDocs = Integer.parseInt(xmlStreamReader.getElementText());
                        }
                        else if ("autn:x1".equals(localName)) {
                            x1 = Integer.parseInt(xmlStreamReader.getElementText());
                        }
                        else if ("autn:x2".equals(localName)) {
                            x2 = Integer.parseInt(xmlStreamReader.getElementText());
                        }
                        else if ("autn:y1".equals(localName)) {
                            y1 = Integer.parseInt(xmlStreamReader.getElementText());
                        }
                        else if ("autn:y2".equals(localName)) {
                            y2 = Integer.parseInt(xmlStreamReader.getElementText());
                        }
                        else if ("autn:id".equals(localName)) {
                            id = Integer.parseInt(xmlStreamReader.getElementText());
                        }
                        break;
                    case XMLEvent.END_ELEMENT:
                        if ("autn:cluster".equals(xmlStreamReader.getLocalName())) {
                            clusters.add(new Cluster(title, jobName, fromDate, toDate, numDocs, x1, x2, y1, y2, id));
                        }
                        break;
                }
            }

            return clusters;
        } catch (XMLStreamException e) {
            throw new ProcessorException("Error reading XML", e);
        }
    }
}

/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.stats;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.util.AciParameters;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.stats.Event;
import com.hp.autonomy.frontend.find.core.stats.StatsService;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.idolutils.processors.AciResponseJaxbProcessorFactory;
import com.hp.autonomy.types.requests.idol.actions.stats.StatsServerActions;
import com.hp.autonomy.types.requests.idol.actions.stats.params.EventParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
@Slf4j
public class IdolStatsService implements StatsService {

    private final BlockingQueue<Event> queue = new LinkedBlockingQueue<>();

    private final AciService statsServerAciService;
    private final AciResponseJaxbProcessorFactory processorFactory;
    private final XmlMapper xmlMapper;
    private final ConfigService<IdolFindConfig> configService;

    @Autowired
    public IdolStatsService(
        final AciService statsServerAciService,
        final AciResponseJaxbProcessorFactory processorFactory,
        final XmlMapper xmlMapper,
        final ConfigService<IdolFindConfig> configService
    ) {
        this.statsServerAciService = statsServerAciService;
        this.processorFactory = processorFactory;
        this.xmlMapper = xmlMapper;
        this.configService = configService;
    }

    @Override
    public void recordEvent(final Event event) {
        // if not enabled, throw the event away
        if (isEnabled()) {
            queue.add(event);
        }
    }

    @Scheduled(fixedRate = 5000L)
    public void drainQueue() {
        final List<Event> eventsList = new ArrayList<>();

        queue.drainTo(eventsList);

        // if no events, no work to do
        // if not enabled, throw the events away
        // (it's still useful to drain the queue if stats are disabled while the app is running)
        if (isEnabled() && !eventsList.isEmpty()) {
            final Events events = new Events(eventsList);

            try {
                final String xml = xmlMapper.writeValueAsString(events);

                final AciParameters parameters = new AciParameters(StatsServerActions.Event.name());
                parameters.put(EventParams.Data.name(), xml);

                statsServerAciService.executeAction(parameters, processorFactory.createEmptyAciResponseProcessor());
            } catch (final JsonProcessingException e) {
                // includes XML errors which should only occur during development
                // throwing won't result in the exception going anywhere useful anyway
                log.error("Error constructing XML: ", e);
            }
        }
    }

    private boolean isEnabled() {
        return configService.getConfig().getStatsServer().isEnabled();
    }
}

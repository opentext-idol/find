/*
 * Copyright 2021 Open Text.
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

package com.hp.autonomy.frontend.find.idol.nifi;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciParameter;
import com.autonomy.aci.client.transport.AciResponseInputStream;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.aci.client.transport.ActionParameter;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.frontend.find.idol.configuration.NifiConfig;
import com.opentext.idol.types.marshalling.ProcessorFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;

import javax.xml.transform.stream.StreamSource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Interact with NiFi.
 */
@Component
public class NifiService {
    private final AciService aciService;
    // one of configService or config is null
    private final ConfigService<IdolFindConfig> configService;
    private final NifiConfig config;
    private final Processor<NifiListActionsResponse> listActionsProcessor;
    private final Processor<Void> successProcessor;

    private NifiService(
        final ProcessorFactory processorFactory,
        final AciService aciService,
        final ConfigService<IdolFindConfig> configService,
        final NifiConfig config
    ) {
        listActionsProcessor = new NifiListActionsResponseProcessor();
        successProcessor = processorFactory.getVoidProcessor();
        this.aciService = aciService;
        this.configService = configService;
        this.config = config;
    }

    @Autowired
    NifiService(
        final ProcessorFactory processorFactory,
        final AciService aciService,
        final ConfigService<IdolFindConfig> configService
    ) {
        this(processorFactory, aciService, configService, null);
    }

    /**
     * Interact with NiFi using a fixed configuration.
     *
     * @param config How to connect to the server
     */
    public NifiService(
        final ProcessorFactory processorFactory,
        final AciService aciService,
        final NifiConfig config
    ) {
        this(processorFactory, aciService, null, config);
    }

    private NifiConfig getConfig() {
        if (config != null) {
            return config;
        } else {
            return configService.getConfig().getNifi();
        }
    }

    private AciServerDetails getAciServerDetails() {
        final NifiConfig config = getConfig();
        if (config == null || !BooleanUtils.isTrue(config.getEnabled())) {
            throw new IllegalArgumentException("NiFi is disabled");
        } else {
            return config.getServer().toAciServerDetails();
        }
    }

    /**
     * Check the API is accessible.
     */
    public void checkStatus() {
        getActions(null, new ArrayList<>());
    }

    /**
     * Retrieve available actions.
     *
     * @param username Name of the user performing the action
     * @param roles Roles of the user performing the action
     */
    public List<NifiAction> getActions(
        final String username,
        final Collection<String> roles
    ) {
        final String listAction = getConfig().getListAction();
        final Set<ActionParameter<?>> params = new HashSet<>();
        params.add(new AciParameter("action", listAction));
        if (username != null) {
            params.add(new AciParameter("username", username));
        }
        params.add(new AciParameter("userRoles", String.join(",", roles)));

        return aciService
            .executeAction(getAciServerDetails(), params, listActionsProcessor)
            .getActions()
            .stream()
            .filter(action -> !listAction.equals(action.getId()))
            .collect(Collectors.toList());
    }

    /**
     * Execute an action on a set of documents.
     *
     * @param action Action ID
     * @param documentsStateToken Defines the document set
     * @param documentsSecurityInfo Used to access the documents, to ensure they're all visible
     * @param username Name of the user performing the action
     * @param roles Roles of the user performing the action
     * @param searchName Name of the search containing the documents - saved with the execution as
     *                   metadata
     * @param label Optional label to save with the execution as metadata
     */
    public void executeAction(
        final String action,
        final String documentsStateToken,
        final String documentsSecurityInfo,
        final String username,
        final Collection<String> roles,
        final String searchName,
        final String label
    ) {
        final Set<ActionParameter<?>> params = new HashSet<>();
        params.add(new AciParameter("action", action));
        params.add(new AciParameter("stateMatchId", documentsStateToken));
        params.add(new AciParameter("securityInfo", documentsSecurityInfo));
        if (username != null) {
            params.add(new AciParameter("username", username));
        }
        params.add(new AciParameter("userRoles", String.join(",", roles)));
        if (searchName != null) {
            params.add(new AciParameter("searchName", searchName));
        }
        if (label != null) {
            params.add(new AciParameter("label", label));
        }
        aciService.executeAction(getAciServerDetails(), params, successProcessor);
    }


    // need a custom processor because we don't have the standard ACI responsedata wrapper
    /**
     * Response processor for the list-actions action.
     */
    private static class NifiListActionsResponseProcessor
        implements Processor<NifiListActionsResponse>
    {
        private static final Jaxb2Marshaller marshaller;

        static {
            marshaller = new Jaxb2Marshaller();
            marshaller.setClassesToBeBound(new Class[]{NifiListActionsResponse.class});
        }

        @Override
        public NifiListActionsResponse process(final AciResponseInputStream inputStream) {
            return (NifiListActionsResponse) marshaller.unmarshal(new StreamSource(inputStream));
        }

    }

}

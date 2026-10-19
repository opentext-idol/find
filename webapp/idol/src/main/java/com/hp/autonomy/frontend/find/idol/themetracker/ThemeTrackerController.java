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

package com.hp.autonomy.frontend.find.idol.themetracker;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.util.ActionParameters;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.idol.api.ThemeTrackerApi;
import com.hp.autonomy.frontend.find.idol.api.model.TermsResponse;
import com.hp.autonomy.frontend.find.idol.api.model.ThemeClustersResponse;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@RestController
class ThemeTrackerController implements ThemeTrackerApi {
    private final ConfigService<IdolFindConfig> configService;
    private final AciService themeTrackerAciService;
    private final HttpServletResponse response;

    @Autowired
    ThemeTrackerController(
            final ConfigService<IdolFindConfig> configService,
            final AciService themeTrackerAciService,
            final HttpServletResponse response) {
        this.configService = configService;
        this.themeTrackerAciService = themeTrackerAciService;
        this.response = response;
    }

    private String getJobName(final String jobName) {
        if (StringUtils.isNotEmpty(jobName)) {
            return jobName;
        }

        return configService.getConfig().getThemeTracker().getJobName();
    }

    @Override
    public ResponseEntity<ThemeClustersResponse> getThemeClusters(
            final Long startDate,
            final Long interval,
            final String jobName) {
        final String effectiveJobName = getJobName(jobName);

        final ActionParameters params = new ActionParameters("ClusterSGDataServe");
        params.add("startdate", startDate);
        params.add("enddate", startDate + interval);
        params.add("sourcejobname", effectiveJobName);
        params.add("StructuredXML", true);

        final List<Cluster> clusters = themeTrackerAciService.executeAction(params, new ClusterStaxProcessor(effectiveJobName));

        return ResponseEntity.ok(new ThemeClustersResponse(clusters));
    }

    @Override
    public ResponseEntity<TermsResponse> getThemeTerms(final Cluster cluster) {
        final ActionParameters params = new ActionParameters("ClusterSGDocsServe");
        params.add("startdate", cluster.fromDate);
        params.add("enddate", cluster.toDate-1);
        params.add("sourcejobname", cluster.jobName);
        params.add("cluster", cluster.id);
        params.add("NumResults", 0);

        return ResponseEntity.ok(
                new TermsResponse(themeTrackerAciService.executeAction(params, new TermsProcessor())));
    }

    @Override
    public ResponseEntity<Resource> getThemeImage(
            final Long startDate,
            final Long interval,
            final String jobName) {
        final ActionParameters params = new ActionParameters("clustersgpicserve");
        params.add("startdate", startDate);
        params.add("enddate", startDate + interval);
        params.add("sourcejobname", getJobName(jobName));

        final ServletOutputStream outputStream;
        try {
            outputStream = response.getOutputStream();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }

        themeTrackerAciService.executeAction(params, (Processor<Boolean>) aciResponse -> {
            try
            {
                response.setContentType(aciResponse.getContentType());
                IOUtils.copyLarge(aciResponse, outputStream);
            } catch (IOException e) {
                throw new ProcessorException("Error fetching image", e);
            }
            return true;
        });

        try {
            outputStream.flush();
            outputStream.close();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }

        // tell Spring the request has already been handled
        return null;
    }
}

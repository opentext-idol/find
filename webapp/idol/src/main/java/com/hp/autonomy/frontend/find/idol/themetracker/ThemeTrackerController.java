/*
 * Copyright 2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.themetracker;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.AciServiceException;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ThemeTrackerController.BASE_PATH)
class ThemeTrackerController {
    static final String BASE_PATH = "/api/public/themetracker";

    private final ConfigService<IdolFindConfig> configService;
    private final AciService themeTrackerAciService;

    @Autowired
    ThemeTrackerController(
            final ConfigService<IdolFindConfig> configService,
            final AciService themeTrackerAciService) {
        this.configService = configService;
        this.themeTrackerAciService = themeTrackerAciService;
    }

    private static final String INTERVAL = "604800";

    private String getJobName(final String jobName) {
        if (StringUtils.isNotEmpty(jobName)) {
            return jobName;
        }

        return configService.getConfig().getThemeTracker().getJobName();
    }

    @RequestMapping(value="/clusters")
    @ResponseBody
    public Map<String, List<Cluster>> themeClusters(
            @RequestParam("startDate") final long startDate,
            @RequestParam(value="interval", defaultValue = INTERVAL) final long interval,
            @RequestParam(value = "jobName", required = false) final String jobName) throws AciServiceException {
        final String effectiveJobName = getJobName(jobName);

        final AciParameters params = new AciParameters("ClusterSGDataServe");
        params.add("startdate", startDate);
        params.add("enddate", startDate + interval);
        params.add("sourcejobname", effectiveJobName);
        params.add("StructuredXML", true);

        final List<Cluster> clusters = themeTrackerAciService.executeAction(params, new ClusterStaxProcessor(effectiveJobName));

        return Collections.singletonMap("clusters", clusters);
    }

    @RequestMapping(value="/terms")
    @ResponseBody
    public List<String> terms(
            @RequestBody final Cluster cluster) throws AciServiceException {
        final AciParameters params = new AciParameters("ClusterSGDocsServe");
        params.add("startdate", cluster.fromDate);
        params.add("enddate", cluster.toDate-1);
        params.add("sourcejobname", cluster.jobName);
        params.add("cluster", cluster.id);
        params.add("NumResults", 0);

        return themeTrackerAciService.executeAction(params, new TermsProcessor());
    }

    @RequestMapping(value="/image")
    public void themeImage(
            @RequestParam("startDate") final long startDate,
            @RequestParam(value="interval", defaultValue = INTERVAL) final long interval,
            @RequestParam(value = "jobName", required = false) final String jobName,
            final HttpServletResponse response) throws AciServiceException, IOException {
        final AciParameters params = new AciParameters("clustersgpicserve");
        params.add("startdate", startDate);
        params.add("enddate", startDate + interval);
        params.add("sourcejobname", getJobName(jobName));

        final ServletOutputStream outputStream = response.getOutputStream();

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

        outputStream.flush();
        outputStream.close();
    }
}

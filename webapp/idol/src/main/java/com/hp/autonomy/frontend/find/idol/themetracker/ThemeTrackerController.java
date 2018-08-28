/*
 * Copyright 2017 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.themetracker;

import com.autonomy.aci.client.services.AciServiceException;
import com.autonomy.visualizers.themetracker.Cluster;
import com.autonomy.visualizers.themetracker.ThemeTracker;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
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

    private final ThemeTracker themeTracker;
    private final ConfigService<IdolFindConfig> configService;

    @Autowired
    ThemeTrackerController(
            final ThemeTracker themeTracker,
            final ConfigService<IdolFindConfig> configService) {
        this.themeTracker = themeTracker;
        this.configService = configService;
    }

    private static final String INTERVAL = "604800";

    private String getJobName(final String jobName) {
        if (StringUtils.isNotEmpty(jobName)) {
            return jobName;
        }

        return configService.getConfig().getThemetracker().getJobName();
    }

    @RequestMapping(value="/clusters")
    @ResponseBody
    public Map<String, List<Cluster>> themeClusters(
            @RequestParam("startDate") final long startDate,
            @RequestParam(value="interval", defaultValue = INTERVAL) final long interval,
            @RequestParam(value = "jobName", required = false) final String jobName) throws AciServiceException {
        final String effectiveJob = getJobName(jobName);
        final List<Cluster> clusters = themeTracker.themeClusters(startDate, interval, effectiveJob);

        for(Cluster cluster : clusters) {
            cluster.jobName = effectiveJob;
        }

        return Collections.singletonMap("clusters", clusters);
    }

    @RequestMapping(value="/terms")
    @ResponseBody
    public List<String> terms(
            @RequestBody final Cluster cluster) throws AciServiceException {
        return themeTracker.themeTimeline(cluster, null, null, 0).getTerms();
    }

    @RequestMapping(value="/image")
    public void themeImage(
            @RequestParam("startDate") final long startDate,
            @RequestParam(value="interval", defaultValue = INTERVAL) final long interval,
            @RequestParam(value = "jobName", required = false) final String jobName,
            final HttpServletResponse response) throws AciServiceException, IOException {
        themeTracker.themeImage(startDate, interval, getJobName(jobName), response);
    }
}

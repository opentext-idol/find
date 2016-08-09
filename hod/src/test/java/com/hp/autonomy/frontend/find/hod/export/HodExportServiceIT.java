/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.export;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.export.CsvExportStrategy;
import com.hp.autonomy.frontend.find.core.export.ExportService;
import com.hp.autonomy.frontend.find.core.export.ExportServiceIT;
import com.hp.autonomy.frontend.find.core.export.ExportStrategy;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.hod.beanconfiguration.HavenSearchHodConfiguration;
import com.hp.autonomy.searchcomponents.hod.configuration.HodSearchCapable;
import com.hp.autonomy.searchcomponents.hod.search.HodSearchResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootTest(classes = {HavenSearchHodConfiguration.class, HodExportServiceIT.ExportConfiguration.class}, properties = "export.it=true", webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class HodExportServiceIT extends ExportServiceIT<ResourceIdentifier, HodErrorException> {
    @Configuration
    @ConditionalOnProperty("export.it")
    public static class ExportConfiguration {
        @Bean
        public ExportService<ResourceIdentifier, HodErrorException> exportService(
                final DocumentsService<ResourceIdentifier, HodSearchResult, HodErrorException> documentsService,
                final ExportStrategy[] exportStrategies) {
            return new HodExportService(documentsService, exportStrategies);
        }

        @Bean
        public ExportStrategy csvExportStrategy(final ConfigService<HodSearchCapable> configService) {
            return new CsvExportStrategy(configService);
        }
    }
}

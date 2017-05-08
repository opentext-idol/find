/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.export.service;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.export.service.CsvExportStrategy;
import com.hp.autonomy.frontend.find.core.export.service.PlatformDataExportService;
import com.hp.autonomy.frontend.find.core.export.service.PlatformDataExportServiceIT;
import com.hp.autonomy.frontend.find.core.export.service.PlatformDataExportStrategy;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.fields.FieldDisplayNameGenerator;
import com.hp.autonomy.searchcomponents.core.fields.FieldPathNormaliser;
import com.hp.autonomy.searchcomponents.hod.beanconfiguration.HavenSearchHodConfiguration;
import com.hp.autonomy.searchcomponents.hod.configuration.HodSearchCapable;
import com.hp.autonomy.searchcomponents.hod.search.HodDocumentsService;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRequest;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootTest(classes = {HavenSearchHodConfiguration.class, HodPlatformDataExportServiceIT.ExportConfiguration.class}, properties = "export.it=true", webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class HodPlatformDataExportServiceIT extends PlatformDataExportServiceIT<HodQueryRequest, HodQueryRestrictions, HodErrorException> {
    @Configuration
    @ConditionalOnProperty("export.it")
    public static class ExportConfiguration {
        @Bean
        public PlatformDataExportService<HodQueryRequest, HodErrorException> exportService(
                final HodDocumentsService documentsService,
                final PlatformDataExportStrategy[] exportStrategies) {
            return new HodPlatformDataExportService(documentsService, exportStrategies);
        }

        @Bean
        public PlatformDataExportStrategy csvExportStrategy(final ConfigService<HodSearchCapable> configService,
                                                            final FieldPathNormaliser fieldPathNormaliser,
                                                            final FieldDisplayNameGenerator fieldDisplayNameGenerator) {
            return new CsvExportStrategy(configService, fieldPathNormaliser, fieldDisplayNameGenerator);
        }
    }
}

/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.export;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.export.CsvExportStrategy;
import com.hp.autonomy.frontend.find.core.export.ExportService;
import com.hp.autonomy.frontend.find.core.export.ExportServiceIT;
import com.hp.autonomy.frontend.find.core.export.ExportStrategy;
import com.hp.autonomy.searchcomponents.idol.beanconfiguration.HavenSearchIdolConfiguration;
import com.hp.autonomy.searchcomponents.idol.configuration.AciServiceRetriever;
import com.hp.autonomy.searchcomponents.idol.configuration.IdolSearchCapable;
import com.hp.autonomy.searchcomponents.idol.search.HavenSearchAciParameterHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@ContextConfiguration(classes = {HavenSearchIdolConfiguration.class, IdolExportServiceIT.ExportConfiguration.class})
@TestPropertySource(properties = "export.it=true")
public class IdolExportServiceIT extends ExportServiceIT<String, AciErrorException> {
    @Configuration
    @ConditionalOnProperty("export.it")
    public static class ExportConfiguration {
        @Bean
        public ExportService<String, AciErrorException> exportService(
                final HavenSearchAciParameterHandler parameterHandler,
                final AciServiceRetriever aciServiceRetriever,
                final ExportStrategy[] exportStrategies) {
            return new IdolExportService(parameterHandler, aciServiceRetriever, exportStrategies);
        }

        @Bean
        public ExportStrategy csvExportStrategy(final ConfigService<IdolSearchCapable> configService) {
            return new CsvExportStrategy(configService);
        }
    }
}

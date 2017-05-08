package com.hp.autonomy.frontend.find.hod.export.service;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.export.service.CsvExportStrategy;
import com.hp.autonomy.frontend.find.core.export.service.ExportServiceFactoryTest;
import com.hp.autonomy.frontend.find.core.export.service.PowerPointExportService;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.config.HavenSearchCapable;
import com.hp.autonomy.searchcomponents.core.fields.FieldDisplayNameGenerator;
import com.hp.autonomy.searchcomponents.core.fields.FieldPathNormaliser;
import com.hp.autonomy.searchcomponents.hod.search.HodDocumentsService;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRequest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SuppressWarnings("unused")
@SpringBootTest(classes = {HodExportServiceFactory.class, PowerPointExportService.class, HodPlatformDataExportService.class, CsvExportStrategy.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class HodExportServiceFactoryTest extends ExportServiceFactoryTest<HodQueryRequest, HodErrorException> {
    @MockBean
    private HodDocumentsService documentsService;
    @MockBean
    private ConfigService<HavenSearchCapable> configService;
    @MockBean
    private FieldPathNormaliser fieldPathNormaliser;
    @MockBean
    private FieldDisplayNameGenerator fieldDisplayNameGenerator;
}

package com.hp.autonomy.frontend.find.idol.export.service;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.export.service.CsvExportStrategy;
import com.hp.autonomy.frontend.find.core.export.service.ExportServiceFactoryTest;
import com.hp.autonomy.frontend.find.core.export.service.PowerPointExportService;
import com.hp.autonomy.searchcomponents.core.config.HavenSearchCapable;
import com.hp.autonomy.searchcomponents.core.fields.FieldDisplayNameGenerator;
import com.hp.autonomy.searchcomponents.core.fields.FieldPathNormaliser;
import com.hp.autonomy.searchcomponents.idol.configuration.AciServiceRetriever;
import com.hp.autonomy.searchcomponents.idol.search.HavenSearchAciParameterHandler;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SuppressWarnings("unused")
@SpringBootTest(classes = {IdolExportServiceFactory.class, PowerPointExportService.class, IdolPlatformDataExportService.class, CsvExportStrategy.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class IdolExportServiceFactoryTest extends ExportServiceFactoryTest<IdolQueryRequest, AciErrorException> {
    @MockitoBean
    private HavenSearchAciParameterHandler parameterHandler;
    @MockitoBean
    private AciServiceRetriever aciServiceRetriever;
    @MockitoBean
    private ConfigService<HavenSearchCapable> configService;
    @MockitoBean
    private FieldPathNormaliser fieldPathNormaliser;
    @MockitoBean
    private FieldDisplayNameGenerator fieldDisplayNameGenerator;
}

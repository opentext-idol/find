package com.hp.autonomy.frontend.find.idol.export.service;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.export.service.ExportServiceFactoryBase;
import com.hp.autonomy.frontend.find.core.export.service.PlatformDataExportService;
import com.hp.autonomy.frontend.find.core.export.service.VisualDataExportService;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
class IdolExportServiceFactory extends ExportServiceFactoryBase<IdolQueryRequest, AciErrorException> {
    @Autowired
    public IdolExportServiceFactory(final Collection<PlatformDataExportService<IdolQueryRequest, AciErrorException>> platformDataExportServices,
                                    final Collection<VisualDataExportService> visualDataExportServices) {
        super(platformDataExportServices, visualDataExportServices);
    }
}

package com.hp.autonomy.frontend.find.hod.web;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.authentication.AuthenticationConfig;
import com.hp.autonomy.frontend.find.core.export.MetadataNode;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.FindController;
import com.hp.autonomy.frontend.find.core.web.MvcConstants;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.frontend.find.hod.export.HodMetadataNode;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.*;

@Controller
public class HodFindController extends FindController<HodFindConfig, HodFindConfig.HodFindConfigBuilder> {

    @SuppressWarnings("TypeMayBeWeakened")
    @Autowired
    public HodFindController(final ControllerUtils controllerUtils,
                             final AuthenticationInformationRetriever<?, ? extends Principal> authenticationInformationRetriever,
                             final ConfigService<? extends AuthenticationConfig<?>> authenticationConfigService,
                             final ConfigService<HodFindConfig> configService) {
        super(controllerUtils, authenticationInformationRetriever, authenticationConfigService, configService);
    }

    @Override
    protected Map<String, Object> getPublicConfig() {
        final HodFindConfig config = configService.getConfig();

        return Collections.singletonMap(MvcConstants.PUBLIC_INDEXES_ENABLED.value(), config.getHod().getPublicIndexesEnabled());
    }

    @Override
    protected List<MetadataNode> getMetadataNodes() {
        return Arrays.asList(HodMetadataNode.values());
    }
}

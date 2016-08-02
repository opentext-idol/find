package com.hp.autonomy.frontend.find.idol.web;

import com.hp.autonomy.frontend.configuration.AuthenticationConfig;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.FindController;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.frontend.find.idol.configuration.MMAP;
import com.hp.autonomy.searchcomponents.core.authentication.AuthenticationInformationRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
public class IdolFindController extends FindController<IdolFindConfig> {
    private static final String MMAP_BASE_URL = "mmapBaseUrl";
    private static final String VIEW_HIGHLIGHTING = "viewHighlighting";

    @Autowired
    protected IdolFindController(final ControllerUtils controllerUtils,
                                 final AuthenticationInformationRetriever<?, ? extends Principal> authenticationInformationRetriever,
                                 final ConfigService<? extends AuthenticationConfig<?>> authenticationConfigService,
                                 final ConfigService<IdolFindConfig> configService) {
        super(controllerUtils, authenticationInformationRetriever, authenticationConfigService, configService);
    }


    @Override
    protected Map<String, Object> getPublicConfig() {
        final Map<String, Object> publicConfig = new HashMap<>();
        final IdolFindConfig config = configService.getConfig();

        final MMAP mmap = config.getMmap();

        if (mmap.isEnabled()) {
            publicConfig.put(MMAP_BASE_URL, mmap.getBaseUrl());
        }

        publicConfig.put(VIEW_HIGHLIGHTING, config.getViewConfig().getHighlighting());

        return publicConfig;
    }
}

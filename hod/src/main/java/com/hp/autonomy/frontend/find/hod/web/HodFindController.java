package com.hp.autonomy.frontend.find.hod.web;

import com.hp.autonomy.frontend.configuration.AuthenticationConfig;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.FindController;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.searchcomponents.core.authentication.AuthenticationInformationRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

@Controller
public class HodFindController extends FindController<HodFindConfig> {

    @Autowired
    public HodFindController(final ControllerUtils controllerUtils,
                             final AuthenticationInformationRetriever<?, ? extends Principal> authenticationInformationRetriever,
                             final ConfigService<? extends AuthenticationConfig<?>> authenticationConfigService,
                             final ConfigService<HodFindConfig> configService) {
        super(controllerUtils, authenticationInformationRetriever, authenticationConfigService, configService);
    }

    @Override
    protected Map<String, Object> getPublicConfig() {
        return Collections.emptyMap();
    }
}

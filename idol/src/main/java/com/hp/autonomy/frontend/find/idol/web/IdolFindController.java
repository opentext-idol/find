package com.hp.autonomy.frontend.find.idol.web;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.web.FindController;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.frontend.find.idol.configuration.MMAP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
public class IdolFindController extends FindController {
    private static final String MMAP_BASE_URL = "mmapBaseUrl";
    private static final String VIEW_HIGHLIGHTING = "viewHighlighting";

    @Autowired
    private ConfigService<IdolFindConfig> configService;

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

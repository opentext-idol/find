package com.hp.autonomy.frontend.find.idol.web;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.web.FindController;
import com.hp.autonomy.frontend.find.core.web.MvcConstants;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.frontend.find.idol.configuration.MMAP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
public class IdolFindController extends FindController {
    private static final String MMAP_BASE_URL = "mmapBaseUrl";

    @Autowired
    private ConfigService<IdolFindConfig> configService;

    @Override
    protected Map<String, Object> getPublicConfig() {
        final Map<String, Object> config = new HashMap<>();
        final MMAP mmap = configService.getConfig().getMmap();

        if (mmap.isEnabled()) {
            config.put(MMAP_BASE_URL, mmap.getBaseUrl());
        }

        return config;
    }
}

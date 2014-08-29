package com.hp.autonomy.frontend.find;

import com.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.configuration.FindConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfigApiKeyService implements ApiKeyService {

    @Autowired
    private ConfigService<FindConfig> configService;

    @Override
    public String getApiKey() {
        return configService.getConfig().getIod().getApiKey();
    }
}

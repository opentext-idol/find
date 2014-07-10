package com.hp.autonomy.frontend.find;

import org.springframework.stereotype.Service;

@Service
public class BasicApiKeyService implements ApiKeyService {
    @Override
    public String getApiKey() {
        return "XYZ123ABC";
    }
}

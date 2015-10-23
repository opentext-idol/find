package com.autonomy.abc.selenium.config.authproviders;

import com.hp.autonomy.frontend.selenium.sso.ApiKey;

public class APIKeyInput extends ApiKey {
    public APIKeyInput() {
        super(System.getProperty("com.autonomy.apikey"));
    }
}

package com.autonomy.abc.selenium.config.authproviders;

import com.hp.autonomy.frontend.selenium.sso.GoogleAuth;

public class Google extends GoogleAuth {
    public Google() {
        super(System.getProperty("com.autonomy.username"), System.getProperty("com.autonomy.password"));
    }
}

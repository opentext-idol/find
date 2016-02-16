package com.autonomy.abc.config;

import com.autonomy.abc.selenium.page.login.GoogleAuth;
import com.autonomy.abc.selenium.users.HSONewUser;
import com.autonomy.abc.selenium.users.NewUser;
import com.autonomy.abc.selenium.util.Factory;

import java.util.UUID;

public class RandomNewUserFactory implements Factory<NewUser> {
    // TODO: remove hardcoded strings
    private final String emailPrefix = "hodtestqa401";
    private final String emailSuffix = "@gmail.com";

    RandomNewUserFactory(TestConfig config) {
    }

    @Override
    public NewUser create() {
        String randomString = getRandomString();
        return new HSONewUser(randomString, gmailString(randomString), getAuthProvider());
    }

    private String getRandomString() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    private String gmailString(String extra) {
        return emailPrefix + "+" + extra + emailSuffix;
    }

    private GoogleAuth getAuthProvider() {
        String password = "qoxntlozubjaamyszerfk";
        return new GoogleAuth(emailPrefix + emailSuffix, password);
    }
}

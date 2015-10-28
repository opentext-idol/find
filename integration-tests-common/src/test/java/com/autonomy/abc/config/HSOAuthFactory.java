package com.autonomy.abc.config;

import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import com.hp.autonomy.frontend.selenium.sso.*;

import java.util.Map;

public enum HSOAuthFactory {
    API_KEY() {
        @Override
        AuthProvider getProvider(Map<String, Object> map) {
            return new ApiKey(map.get("apiKey").toString());
        }
    },
    FACEBOOK() {
        @Override
        AuthProvider getProvider(Map<String, Object> map) {
            return new FacebookAuth(map.get("email").toString(), map.get("password").toString());
        }
    },
    GOOGLE() {
        @Override
        AuthProvider getProvider(Map<String, Object> map) {
            return new GoogleAuth(map.get("email").toString(), map.get("password").toString());
        }
    },
    HP_PASSPORT() {
        @Override
        AuthProvider getProvider(Map<String, Object> map) {
            return new HPPassport(map.get("username").toString(), map.get("password").toString());
        }
    },
    OPEN_ID() {
        @Override
        AuthProvider getProvider(Map<String, Object> map) {
            return new OpenID(map.get("url").toString());
        }
    },
    TWITTER() {
        @Override
        AuthProvider getProvider(Map<String, Object> map) {
            return new TwitterAuth(map.get("username").toString(), map.get("password").toString());
        }
    },
    YAHOO() {
        @Override
        AuthProvider getProvider(Map<String, Object> map) {
            return new YahooAuth(map.get("username").toString(), map.get("password").toString());
        }
    };

    private static HSOAuthFactory getFactory(String string) {
        return HSOAuthFactory.valueOf(string.toUpperCase().replace(' ', '_'));
    }

    abstract AuthProvider getProvider(Map<String, Object> map);

    public static AuthProvider fromMap(Map<String, Object> map) {
        HSOAuthFactory factory = getFactory(map.get("type").toString());
        return factory.getProvider(map);
    }
}

package com.hp.autonomy.frontend.selenium.config.json;

import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class AppConfig {
    private final ApplicationType type;
    private final Map<String, URL> urls;

    AppConfig(JsonNode node) throws MalformedURLException {
        type = readType(node.path("type"));
        urls = readUrls(node.fields());
    }

    private AppConfig(AppConfig overrides, AppConfig defaults) {
        type = JsonConfigHelper.override(defaults.type, overrides.type);
        urls = JsonConfigHelper.mapOverride(defaults.urls, overrides.urls);
    }

    AppConfig overrideUsing(AppConfig overrides) {
        return overrides == null ? this : new AppConfig(overrides, this);
    }

    private ApplicationType readType(JsonNode node) {
        String typeString = node.asText();
        return typeString.isEmpty() ? null : ApplicationType.fromString(typeString);
    }

    private Map<String, URL> readUrls(Iterator<Map.Entry<String, JsonNode>> entries) throws MalformedURLException {
        Map<String, URL> urls = new HashMap<>();
        while (entries.hasNext()) {
            Map.Entry<String, JsonNode> entry = entries.next();
            if (!entry.getKey().equals("type")) {
                urls.put(entry.getKey(), JsonConfigHelper.getUrlOrNull(entry.getValue()));
            }
        }
        return urls;
    }

    ApplicationType getType() {
        return type;
    }

    URL getUrl(String appName) {
        return urls.get(appName);
    }

    @Override
    public String toString() {
        return "{type=" + type + ", urls=" + urls + "}";
    }

}

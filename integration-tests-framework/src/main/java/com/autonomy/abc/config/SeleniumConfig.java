package com.autonomy.abc.config;

import com.autonomy.abc.selenium.control.Resolution;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class SeleniumConfig {
    private final URL url;
    private final List<Browser> browsers;
    private final Resolution resolution;
    private final int timeout;

    SeleniumConfig(JsonNode node) throws MalformedURLException {
        url = JsonConfigHelper.getUrlOrNull(node.path("url"));
        browsers = readBrowsers(node.path("browsers"));
        resolution = readResolution(node.path("resolution"));
        timeout = node.path("timeout").asInt(-1);
    }

    private SeleniumConfig(SeleniumConfig overrides, SeleniumConfig defaults) {
        url = JsonConfigHelper.override(defaults.getUrl(), overrides.getUrl());
        browsers = JsonConfigHelper.override(defaults.getBrowsers(), overrides.getBrowsers());
        resolution = JsonConfigHelper.override(defaults.getResolution(), overrides.getResolution());
        timeout = (overrides.getTimeout() > 0) ? overrides.getTimeout() : defaults.getTimeout();
    }

    SeleniumConfig overrideUsing(SeleniumConfig overrides) {
        return overrides == null ? this : new SeleniumConfig(overrides, this);
    }

    private List<Browser> readBrowsers(JsonNode browsersNode) {
        if (browsersNode.isMissingNode()) {
            return null;
        }
        List<Browser> browsers = new ArrayList<>();
        for (JsonNode browserNode : browsersNode) {
            browsers.add(Browser.fromString(browserNode.asText()));
        }
        return Collections.unmodifiableList(browsers);
    }

    private Resolution readResolution(JsonNode resolutionNode) {
        if (resolutionNode.isMissingNode()) {
            return null;
        }
        int width = resolutionNode.get(0).asInt();
        int height = resolutionNode.get(1).asInt();
        return new Resolution(width, height);
    }

    URL getUrl() {
        return url;
    }

    List<Browser> getBrowsers() {
        return browsers;
    }

    Resolution getResolution() {
        return resolution;
    }

    int getTimeout() {
        return timeout;
    }

    @Override
    public String toString() {
        return "{browsers=" + getBrowsers() + ", url=" + getUrl() + ", resolution=" + getResolution() + ", timeout=" + getTimeout() + "}";
    }
}

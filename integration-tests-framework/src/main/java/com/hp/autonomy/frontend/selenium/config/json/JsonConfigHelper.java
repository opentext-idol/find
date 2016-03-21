package com.hp.autonomy.frontend.selenium.config.json;

import com.fasterxml.jackson.databind.JsonNode;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

final class JsonConfigHelper {
    private JsonConfigHelper() {}

    static URL getUrlOrNull(JsonNode node) throws MalformedURLException {
        return node.isMissingNode() ? null : new URL(node.asText());
    }

    static <T> T override(T fallback, T preferred) {
        return preferred == null ? fallback : preferred;
    }

    static <K, V> Map<K, V> mapOverride(Map<K, V> fallback, Map<K, V> preferred) {
        Map<K, V> newMap = new HashMap<>();
        if (fallback != null) {
            newMap.putAll(fallback);
        }
        if (preferred != null) {
            newMap.putAll(preferred);
        }
        return newMap;
    }
}

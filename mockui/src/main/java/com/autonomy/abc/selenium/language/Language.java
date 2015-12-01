package com.autonomy.abc.selenium.language;

import java.util.HashMap;
import java.util.Map;

// hardcoded strings can be problematic
// add new languages here as necessary
public enum Language {
    AFRIKAANS("Afrikaans"),
    ALBANIAN("Albanian"),
    ARABIC("Arabic"),
    AZERI("Azeri"),
    CHINESE("Chinese"),
    CROATIAN("Croatian"),
    ENGLISH("English"),
    FRENCH("French"),
    GEORGIAN("Georgian"),
    GERMAN("German"),
    HINDI("Hindi"),
    KAZAKH("Kazakh"),
    KOREAN("Korean"),
    SWAHILI("Swahili"),
    URDU("Urdu"),
    DEFAULT("Select Language"),
    UNKNOWN("???");

    private final static Map<String, Language> inverse;
    private String name;

    static {
        inverse = new HashMap<>();
        for (Language language : Language.values()) {
            inverse.put(language.toString().toLowerCase(), language);
        }
    }

    Language(String name) {
        this.name = name;
    }

    static Language fromString(String language) {
        Language found = inverse.get(language.toLowerCase());
        return found == null ? UNKNOWN : found;
    }

    @Override
    public String toString() {
        return name;
    }
}

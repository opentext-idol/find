package com.autonomy.abc.selenium.util;

public class MatchersUtil {

    public static String cleanXpathString(final String dirtyString) {
        final String cleanString;
        if (dirtyString.contains("'")) {
            cleanString = "concat(\"" + dirtyString.replace("'", "\", \"'\", \"") + "\")";
        } else {
            cleanString = '\'' + dirtyString + '\'';
        }
        return cleanString;
    }
}

package com.autonomy.abc.selenium.util;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public final class Locator extends By {
    private final List<String> classNames = new ArrayList<>();
    private String tagName;
    private String textContent;
    private boolean caseInsensitive;

    public Locator() {
        this.tagName = "*";
        this.textContent = "";
        this.caseInsensitive = false;
    }

    @Override
    public List<WebElement> findElements(SearchContext context) {
        return context.findElements(By.xpath(".//" + tagName + convertedClasses() + "[contains(" + convertedText() + ", " + cleanXpathString(textContent) + ")]"));
    }

    public Locator withTagName(String tagName) {
        this.tagName = tagName;
        return this;
    }

    public Locator havingClass(String className) {
        this.classNames.add(className);
        return this;
    }

    public Locator containingText(String substring) {
        this.textContent = substring;
        return this;
    }

    public Locator containingCaseInsensitive(String substring) {
        this.caseInsensitive = true;
        this.textContent = substring.toLowerCase();
        return this;
    }

    private String convertedClasses() {
        List<String> convertedClasses = new ArrayList<>();
        for (String className : classNames) {
            convertedClasses.add(convertedClass(className));
        }
        return StringUtils.join(convertedClasses, "");
    }

    private String convertedClass(String className) {
        return "[contains(@class,'" + className + "')]";
    }

    private String convertedText() {
        if (caseInsensitive) {
            String stripped = textContent.replace("\'", "").replace(" ", "");
            return "translate(text(), '" + stripped.toUpperCase() + "', '" + stripped.toLowerCase() + "')";
        } else {
            return "text()";
        }
    }

    private static String cleanXpathString(String unclean) {
        String cleanString;
        if(unclean.contains("\'")) {
            cleanString = "concat(\"" + unclean.replace("\'", "\", \"\'\", \"") + "\")";
        } else {
            cleanString = '\'' + unclean + '\'';
        }
        return cleanString;
    }
}

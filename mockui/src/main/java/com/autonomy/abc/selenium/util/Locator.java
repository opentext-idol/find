package com.autonomy.abc.selenium.util;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.List;

public final class Locator extends By {
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
        return context.findElements(By.xpath(".//" + tagName + "[contains(" + convertedText() + ", " + cleanXpathString(textContent) + ")]"));
    }

    public Locator withTagName(String tagName) {
        this.tagName = tagName;
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

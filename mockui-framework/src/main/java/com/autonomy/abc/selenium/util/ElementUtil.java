package com.autonomy.abc.selenium.util;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.util.*;

public final class ElementUtil {
    private ElementUtil() {}

    public static List<String> getTexts(List<? extends WebElement> elements) {
        final List<String> texts = new ArrayList<>();
        for (WebElement element : elements) {
            texts.add(element.getText());
        }
        return texts;
    }

    public static WebElement ancestor(WebElement element, int levels) {
        String xpath = "." + StringUtils.repeat("/..", levels);
        return element.findElement(By.xpath(xpath));
    }

    public static boolean isEnabled(WebElement element) {
        return !isDisabled(element);
    }

    public static boolean isDisabled(WebElement element) {
        return isAttributePresent(element, "disabled") || hasClass("disabled", element);
    }

    public static boolean hasClass(final String className, final WebElement element) {
        final Set<String> classes = getClassSet(element);

        return classes.contains(className);
    }

    private static Set<String> getClassSet(final WebElement element) {
        final Set<String> output = new HashSet<>();
        final String classAttribute = element.getAttribute("class");

        if (classAttribute != null && !classAttribute.isEmpty()) {
            output.addAll(Arrays.asList(classAttribute.split(" +")));
        }

        return output;
    }

    public static void tryClickThenTryParentClick(final WebElement element) {
        try {
            element.click();
        } catch (final WebDriverException e) {
            ancestor(element, 1).click();
        }
    }

    public static boolean isAttributePresent(final WebElement element, final String attribute) {
        boolean result = false;
        try {
            if (element.getAttribute(attribute) != null) {
                result = true;
            }
        } catch (final Exception e) { /* NOOP */ }

        return result;
    }

    public static WebElement getParent(final WebElement child) {
        return ancestor(child, 1);
    }

    public static WebElement getFirstChild(final WebElement parent) {
        return parent.findElement(By.xpath(".//*"));
    }

}

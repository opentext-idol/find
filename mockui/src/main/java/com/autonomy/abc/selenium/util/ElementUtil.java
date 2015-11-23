package com.autonomy.abc.selenium.util;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

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
        String xpath = "./" + StringUtils.repeat("/..", levels);
        return element.findElement(By.xpath(xpath));
    }
}

package com.autonomy.abc.selenium.keywords;

import com.hp.autonomy.frontend.selenium.element.LabelBox;
import com.hp.autonomy.frontend.selenium.element.Removable;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class KeywordsContainer extends AppElement {
    public KeywordsContainer(final WebElement element, final WebDriver driver) {
        super(element, driver);
    }

    public SynonymGroup synonymGroupContaining(final String synonym) {
        final WebElement termBox = findElement(By.cssSelector("[data-term='" + synonym.toLowerCase() + "']"));
        return new SynonymGroup(ElementUtil.ancestor(termBox, 2), getDriver());
    }

    public List<SynonymGroup> synonymGroups() {
        final List<SynonymGroup> groups = new ArrayList<>();
        for (final WebElement child : findElements(By.cssSelector("li .add-synonym-wrapper"))) {
            final WebElement group = ElementUtil.ancestor(child, 1);
            groups.add(new SynonymGroup(group, getDriver()));
        }
        return groups;
    }

    public List<WebElement> keywordGroups() {
        return findElements(By.cssSelector(".keywords-sub-list"));
    }

    public List<WebElement> blacklistTerms() {
        return findElements(By.cssSelector(".blacklisted-word"));
    }

    public List<Removable> keywords() {
        final List<Removable> keywords = new ArrayList<>();
        for (final WebElement element : keywordElements()) {
            keywords.add(new LabelBox(element, getDriver()));
        }
        return keywords;
    }

    public List<String> getKeywords() {
        return ElementUtil.getTexts(keywordElements());
    }

    private List<WebElement> keywordElements() {
        return findElements(By.cssSelector("[data-term]"));
    }

    public List<String> getFirstKeywords(){
        return ElementUtil.getTexts(firstKeywordElements());
    }

    private List<WebElement> firstKeywordElements(){
        return findElements(By.xpath("//*[contains(@class,'keywords-sub-list')]/*[1]"));
    }
}

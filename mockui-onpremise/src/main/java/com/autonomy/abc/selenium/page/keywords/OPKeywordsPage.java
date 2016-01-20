 package com.autonomy.abc.selenium.page.keywords;

 import com.autonomy.abc.selenium.language.LanguageDropdown;
 import com.autonomy.abc.selenium.language.OPLanguageDropdown;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;

public class OPKeywordsPage extends KeywordsPage {
    public OPKeywordsPage(WebDriver driver) {
        super(driver);
    }

    @Override
    protected LanguageDropdown languageDropdown() {
        return new OPLanguageDropdown(findElement(By.cssSelector(".languages-select-view-container .dropdown:nth-of-type(2)")), getDriver());
    }
}

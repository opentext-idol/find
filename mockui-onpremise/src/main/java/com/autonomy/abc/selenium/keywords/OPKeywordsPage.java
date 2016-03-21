 package com.autonomy.abc.selenium.keywords;

 import com.autonomy.abc.selenium.language.LanguageDropdown;
 import com.autonomy.abc.selenium.language.OPLanguageDropdown;
 import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;

public class OPKeywordsPage extends KeywordsPage {
    private OPKeywordsPage(WebDriver driver) {
        super(driver);
    }

    @Override
    protected LanguageDropdown languageDropdown() {
        return new OPLanguageDropdown(findElement(By.cssSelector(".languages-select-view-container .dropdown:nth-of-type(2)")), getDriver());
    }

    public static class Factory implements ParametrizedFactory<WebDriver, OPKeywordsPage> {
        public OPKeywordsPage create(WebDriver context) {
            return new OPKeywordsPage(context);
        }
    }
}

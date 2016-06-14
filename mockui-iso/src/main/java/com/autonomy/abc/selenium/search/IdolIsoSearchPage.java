package com.autonomy.abc.selenium.search;

import com.autonomy.abc.selenium.indexes.IdolDatabaseTree;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.autonomy.abc.selenium.language.IdolLanguageDropdown;
import com.autonomy.abc.selenium.language.LanguageDropdown;
import com.hp.autonomy.frontend.selenium.element.Pagination;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class IdolIsoSearchPage extends SearchPage {
    private IdolIsoSearchPage(final WebDriver driver) {
        super(driver);
    }

    @Override
    protected LanguageDropdown languageDropdown() {
        return new IdolLanguageDropdown(findElement(By.cssSelector(".search-language")), getDriver());
    }

    public WebElement promotionsLabel() {
        return findElement(By.cssSelector(".promotions .promotion-name"));
    }

    public boolean promotionsLabelsExist(){
        return findElements(By.cssSelector(".promotions .promotion-name")).size()>0;
    }
    @Override
    public IndexesTree indexesTree() {
        return new IdolDatabaseTree.Factory().create(allIndexes());
    }

    public List<String> getPromotionLabels() {
        waitForPromotionsLoadIndicatorToDisappear();
        final List<String> labelList = new ArrayList<>();

        if (showMorePromotionsButton().isDisplayed()) {
            showMorePromotions();
        }
        labelList.addAll(getPromotionTypeLabels());

        while (ElementUtil.isEnabled(promotionPaginationButton(Pagination.NEXT))) {
            switchPromotionPage(Pagination.NEXT);
            labelList.addAll(getPromotionTypeLabels());
        }
        return labelList;
    }

    private List<String> getPromotionTypeLabels() {
        return ElementUtil.getTexts(findElements(By.className("promotion-name")));
    }

    public static class Factory extends SOPageFactory<IdolIsoSearchPage> {
        public Factory() {
            super(IdolIsoSearchPage.class);
        }

        public IdolIsoSearchPage create(final WebDriver context) {
            return new IdolIsoSearchPage(context);
        }
    }
}

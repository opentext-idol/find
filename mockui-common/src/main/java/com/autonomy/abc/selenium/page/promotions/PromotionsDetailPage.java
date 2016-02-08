package com.autonomy.abc.selenium.page.promotions;

import com.autonomy.abc.selenium.element.*;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Locator;
import com.autonomy.abc.selenium.util.ParametrizedFactory;
import com.autonomy.abc.selenium.util.Waits;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class PromotionsDetailPage extends AppElement implements AppPage {
    protected PromotionsDetailPage(WebDriver driver) {
        super(new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.className("wrapper-content"))), driver);
        waitForLoad();
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(getDriver(), 10)
                .withMessage("Failed to load Promotions Detail Page")
                .until(ExpectedConditions.visibilityOfElementLocated(By.className("promotion-match-terms")));
    }

    public Editable promotionTitle() {
        return new InlineEdit(findElement(By.className("promotion-title-edit")), getDriver());
    }

    public String getPromotionType() {
        return findElement(By.cssSelector(".promotion-view-name")).getText();
    }

    public Dropdown spotlightTypeDropdown() {
        return new Dropdown(findElement(By.className("promotion-view-name-dropdown")), getDriver());
    }

    public Editable pinPosition() {
        final WebElement group = ElementUtil.getParent(findElement(By.cssSelector(".promotion-position-edit")));
        return new Editable() {
            @Override
            public String getValue() {
                return group.getText();
            }

            @Override
            public WebElement editButton() {
                return group.findElement(By.cssSelector(".inline-edit-open-form"));
            }

            @Override
            public void setValueAsync(String value) {
                int initialValue = Integer.valueOf(getValue());
                editButton().click();
                int desiredValue = Integer.valueOf(value);
                changeValue(initialValue, desiredValue);
                findElement(By.cssSelector(".hp-check")).click();
            }

            private void changeValue(int initialValue, int desiredValue) {
                WebElement button;
                int repeats;
                if (desiredValue > initialValue) {
                    button = group.findElement(By.cssSelector(".plus"));
                    repeats = desiredValue - initialValue;
                } else {
                    button = group.findElement(By.cssSelector(".minus"));
                    repeats = initialValue - desiredValue;
                }
                for (int i=0; i<repeats; i++) {
                    button.click();
                }
            }

            @Override
            public void setValueAndWait(String value) {
                setValueAsync(value);
                waitForUpdate();
            }

            @Override
            public void waitForUpdate() {
                new WebDriverWait(getDriver(), 20).withMessage("waiting for pin position to update").until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".promotion-position-edit .fa-refresh")));
            }

            @Override
            public WebElement getElement() {
                return group;
            }
        };
    }

    public String getLanguage() {
        return findElement(By.className("promotion-language")).getText();
    }

    public WebElement addMoreButton() {
        return findElement(By.className("add-more-promoted-documents"));
    }

    public List<WebElement> dynamicPromotedList(){
        return new WebDriverWait(getDriver(),10).until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".query-search-results div:not(.hide)>h3")));
    }

    public List<String> getDynamicPromotedTitles(){
        waitForDynamicLoadIndicatorToDisappear();
        final List<String> docTitles = new ArrayList<>(getVisibleDynamicPromotedTitles());

        while (ElementUtil.isEnabled(promotedQueryPaginationButton(Pagination.NEXT))) {
            switchPromotedQueryPage(Pagination.NEXT);
            docTitles.addAll(getVisibleDynamicPromotedTitles());
        }

        return docTitles;
    }

    private List<String> getVisibleDynamicPromotedTitles() {
        final List<String> docTitles = new ArrayList<>();

        for (final WebElement docTitle : dynamicPromotedList()) {
            if(!docTitle.getText().equals("Search for something...")) {
                docTitles.add(docTitle.getText());
            }
        }

        return docTitles;
    }

    public List<String> getPromotedTitles() {
        waitForPromotedTitlesToLoad();
        return ElementUtil.getTexts(promotedList());
    }

    private List<WebElement> promotedList() {
        return new WebDriverWait(getDriver(), 10).until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".promoted-documents-list h3")));
    }

    private void waitForPromotedTitlesToLoad() {
        new WebDriverWait(getDriver(), 20).until(ExpectedConditions.refreshed(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                List<WebElement> docs = input.findElements(By.cssSelector(".promoted-documents-list h3"));
                return !(docs.isEmpty() || docs.get(0).getText().contains("Unknown Document"));
            }
        }));
    }

    private WebElement promotedQueryPaginationButton(Pagination pagination) {
        return pagination.findInside(promotedQueryPagination());
    }

    private WebElement promotedQueryPagination() {
        return findElement(By.cssSelector(".query-search-results .pagination-nav"));
    }

    private void switchPromotedQueryPage(Pagination pagination) {
        promotedQueryPaginationButton(pagination).click();
        waitForDynamicLoadIndicatorToDisappear();
    }

    private void waitForDynamicLoadIndicatorToDisappear() {
        Waits.loadOrFadeWait();
        new WebDriverWait(getDriver(), 30)
                .withMessage("Dynamic promoted results didn't load")
                .until(ExpectedConditions.invisibilityOfElementLocated(By.className("fa-spin")));
    }

    public WebElement promotedDocument(final String title) {
        return ElementUtil.ancestor(findElement(new Locator().withTagName("a").containingText(title)), 2);
    }

    public Removable removablePromotedDocument(final String title) {
        return new LabelBox(promotedDocument(title), getDriver());
    }

    public Editable staticPromotedDocumentTitle() {
        return new InlineEdit(findElement(By.className("static-promotion-title-edit")), getDriver());
    }

    public Editable staticPromotedDocumentContent() {
        return new InlineEdit(findElement(By.className("static-promotion-content-edit")), getDriver());
    }

    public Editable queryText() {
        return new InlineEdit(findElement(By.className("promotion-query-edit")), getDriver());
    }

    public void viewDocument(String title) {
        for(WebElement document : promotedList()){
            if(document.getText().equals(title)){
                document.click();
            }
        }
    }

    public PromotionsDetailTriggerForm getTriggerForm() {
        return new PromotionsDetailTriggerForm(findElement(By.className("promotion-match-terms-wrapper")), getDriver());
    }

    public static class Factory implements ParametrizedFactory<WebDriver, PromotionsDetailPage> {
        @Override
        public PromotionsDetailPage create(WebDriver context) {
            return new PromotionsDetailPage(context);
        }
    }
}

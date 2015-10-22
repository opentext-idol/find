package com.autonomy.abc.selenium.page.promotions;

import com.autonomy.abc.selenium.element.*;
import com.autonomy.abc.selenium.promotions.Promotion;
import com.autonomy.abc.selenium.util.Predicates;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PromotionsDetailPage extends AppElement implements AppPage {
    private final static By TRIGGERS = By.cssSelector(".promotion-view-match-terms .term");

    public PromotionsDetailPage(WebDriver driver) {
        super(new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.className("wrapper-content"))), driver);
        waitForLoad();
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOfElementLocated(By.className("promotion-match-terms")));
    }

    // no longer exists, CSA-1619
    @Deprecated
    public WebElement backButton() {
        return findElement(By.xpath(".//a[text()[contains(., 'Back')]]"));
    }

    public Dropdown editMenu() {
        return new Dropdown(findElement(By.className("extra-functions")), getDriver());
    }

    @Deprecated
    public void delete() {
        System.err.println("PromotionDetailsPage.delete no longer works, see CSA-1619");
        final Dropdown editMenu = editMenu();
        editMenu.open();
        editMenu.getItem("Delete").click();
        final ModalView deleteModal = ModalView.getVisibleModalView(getDriver());
        deleteModal.findElement(By.cssSelector(".btn-danger")).click();
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
        return new InlineEdit(findElement(By.className("promotion-position-edit")), getDriver());
    }

    public String getLanguage() {
        return findElement(By.className("promotion-language")).getText();
    }

    public List<String> getTriggerList() {
        final List<String> triggers = new ArrayList<>();
        for (final WebElement trigger : findElements(TRIGGERS)) {
            triggers.add(trigger.getAttribute("data-id"));
        }
        return triggers;
    }

    public List<Removable> triggers() {
        final List<Removable> triggers = new ArrayList<>();
        for (final WebElement trigger : findElements(By.cssSelector(".promotion-view-match-terms .term"))) {
            triggers.add(new LabelBox(trigger, getDriver()));
        }
        return triggers;
    }

    public Removable trigger(final String triggerName) {
        return new LabelBox(findElement(By.cssSelector(".promotion-view-match-terms [data-id='" + triggerName + "']")), getDriver());
    }

    public FormInput triggerAddBox() {
        return new FormInput(findElement(By.cssSelector(".promotion-match-terms [name='words']")), getDriver());
    }

    public WebElement triggerAddButton() {
        return findElement(By.cssSelector(".promotion-match-terms [type='submit']"));
    }

    public String getTriggerError() {
        try {
            return findElement(By.cssSelector(".promotion-match-terms .help-block")).getText();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public void waitForTriggerRefresh() {
        new WebDriverWait(getDriver(), 20).until(Predicates.invisibilityOfAllElementsLocated(By.cssSelector(".promotion-view-match-terms .term .fa-spin")));
    }

    public void addTrigger(String text) {
        triggerAddBox().setAndSubmit(text);
        waitForTriggerRefresh();
    }

    public WebElement addMoreButton() {
        return findElement(By.className("add-more-promoted-documents"));
    }

    public List<WebElement> promotedList() {
        return new WebDriverWait(getDriver(), 10).until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".promoted-documents-list h3")));
    }

    public List<WebElement> dynamicPromotedList(){
        return new WebDriverWait(getDriver(),10).until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".query-search-results div:not(.hide)>h3")));

    }

    public List<String> getDynamicPromotedTitles(){
        final List<String> docTitles = new ArrayList<>();

        do {
            for (final WebElement docTitle : dynamicPromotedList()) {
                docTitles.add(docTitle.getText());
            }
        } while(clickForwardButton());

        return docTitles;
    }

    public List<String> getPromotedTitles() {
        final List<String> docTitles = new ArrayList<>();

        do {
            for (final WebElement docTitle : promotedList()) {
                docTitles.add(docTitle.getText());
            }
        } while(clickForwardButton());

        return docTitles;
    }

    private WebElement forwardButton(){
        return findElement(By.cssSelector(".query-search-results .fa-angle-right"));
    }

    private boolean clickForwardButton(){
        try {
            if(!forwardButton().findElement(By.xpath(".//../..")).getAttribute("class").contains("disabled")) {
                forwardButton().click();
                loadOrFadeWait();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public WebElement promotedDocument(final String title) {
        return findElement(By.cssSelector("ul.promoted-documents-list")).findElement(By.xpath(".//a[contains(text(), '" + title + "')]/../.."));
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

}

package com.autonomy.abc.selenium.indexes;

import com.hp.autonomy.frontend.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.icma.ICMAPageBase;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class IndexesDetailPage extends ICMAPageBase {
    private IndexesDetailPage(WebDriver driver) {
        super(driver);
    }

    public String getIndexHeader(){
        return getDriver().findElement(By.cssSelector("h1 b")).getText();
    }

    public String getIndexTitle() {
        return findElement(By.className("index-md-name")).getText();
    }

    public String getCreatedDate() {
        return findElement(By.className("index-md-create-date")).getText();
    }

    private WebElement button(String buttonText){
        return findElement(By.xpath("//div[contains(@class,'affix-element')]//a[text()[contains(.,'" + buttonText + "')]]"));
    }

    public void deleteIndex() {
        deleteButton().click();
        Waits.loadOrFadeWait();
        confirmDeleteButton().click();
    }

    private WebElement deleteButton(){
        return button("Delete");
    }

    private WebElement confirmDeleteButton() {
        return getDriver().findElement(By.cssSelector("#confirmDeleteBtns [type='submit']")); //Outside of page
    }

    private WebElement addURLInput(){
        return findElement(By.name("urlInput"));
    }

    public void addSiteToIndex(String url){
        // http automatically included by input box
        if (url.startsWith("http://")) {
            url = url.substring(7);
        }
        WebElement inputBox = addURLInput();
        inputBox.sendKeys(url);
        inputBox.findElement(By.xpath(".//..//i")).click();
        waitForSiteToIndex(url);
    }

    private void waitForSiteToIndex(String url) {
        String fullUrl = url.startsWith("http://") ? url : "http://" + url;
        new WebDriverWait(getDriver(),30).until(GritterNotice.notificationContaining("Document \"" + fullUrl + "\" was uploaded successfully"));
    }

    public String sizeString() {
        return findElement(By.className("index-md-docs")).getText();
    }

    public WebElement newConnectionButton() {
        return findElement(By.xpath("//button[text()='New connection']"));
    }

    public List<String> getAssociatedConnectors() {
        List<String> connectors = new ArrayList<>();

        for(WebElement associatedConnector : findElements(By.cssSelector(".connectorsTableContainer table td.text-left .pipeline-name"))){
            connectors.add(associatedConnector.getText());
        }

        return connectors;
    }

    public WebElement filesIngestedGraph() {
        return ElementUtil.ancestor(findElement(By.cssSelector("[for='filesIngestedOption']")),1).findElement(By.cssSelector("wait-for-promise>div>div"));
    }

    public WebElement searches() {
        return ElementUtil.ancestor(findElement(By.xpath("//*[text()='Searches']")), 2);
    }

    public WebElement backButton() {
        return findElement(By.cssSelector("div:not(.affix-clone)>div>#nav-back"));
    }

    public static class Factory implements ParametrizedFactory<WebDriver, IndexesDetailPage> {
        @Override
        public IndexesDetailPage create(WebDriver context) {
            new WebDriverWait(context, 30).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[text()[contains(.,'Index fields')]]")));
            return new IndexesDetailPage(context);
        }
    }
}

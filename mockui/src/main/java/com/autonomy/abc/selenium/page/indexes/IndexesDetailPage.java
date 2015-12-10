package com.autonomy.abc.selenium.page.indexes;

import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.page.SAASPageBase;
import com.autonomy.abc.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class IndexesDetailPage extends SAASPageBase {
    public IndexesDetailPage(WebDriver driver) {
        super(driver);
    }

    public static IndexesDetailPage make(WebDriver driver) {
        new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[text()[contains(.,'Index fields')]]")));
        return new IndexesDetailPage(driver);
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

    public WebElement deleteButton(){
        return button("Delete");
    }

    public WebElement confirmDeleteButton() {
        return getDriver().findElement(By.cssSelector("#confirmDeleteBtns [type='submit']")); //Outside of page
    }

    private WebElement addURLInput(){
        return findElement(By.name("urlInput"));
    }

    public void addSiteToIndex(String url){
        WebElement inputBox = addURLInput();
        inputBox.sendKeys(url);
        inputBox.findElement(By.xpath(".//..//i")).click();
    }

    public void waitForSiteToIndex(String url) {
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
}

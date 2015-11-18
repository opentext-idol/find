package com.autonomy.abc.selenium.page.connections.wizard;

import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.page.SAASPageBase;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by avidan on 10-11-15.
 */
public class ConnectorConfigStepTab extends SAASPageBase {
    private ConnectorConfigStepTab(WebDriver driver) {
        super(driver);
    }

    public static ConnectorConfigStepTab make(WebDriver driver){
        new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.name("connectorConfigStepForm")));
        return new ConnectorConfigStepTab(driver);
    }

    public AppElement scheduleForm(){
        return new AppElement(findElement(By.name("scheduleForm")), getDriver());
    }

    public FormInput timeIntervalInput(){
        return new FormInput(findElement(By.name("timeInterval")), getDriver());
    }

    public AppElement repeatingForm(){
        return new AppElement(findElement(By.name("limitForm")), getDriver());
    }

    public AppElement unlimitedOccurrencesCheckBox(){
        return new AppElement(findElement(By.name("no-limit")), getDriver());
    }

    public AppElement limitedOccurrencesCheckBox(){
        return new AppElement(findElement(By.name("limit-occurrences")), getDriver());
    }

    public FormInput occurrencesInput(){
        return new FormInput(findElement(By.name("occurrences")), getDriver());
    }

    public WebElement advancedConfigurations(){
        return findElement(By.id("advancedConfigurationPropsHeader"));
    }

    public FormInput getDepthBox(){
        return new FormInput(findElement(By.name("depth")), getDriver());
    }

    public FormInput getMaxPagesBox() {
        return new FormInput(findElement(By.name("max_pages")), getDriver());
    }

    public FormInput getDurationBox() {
        return new FormInput(findElement(By.name("duration")), getDriver());
    }

    public FormInput getMaxLinksBox() {
        return new FormInput(findElement(By.name("max_links_per_page")), getDriver());
    }

    public FormInput getMaxPageSizeBox() {
        return new FormInput(findElement(By.name("max_page_size")), getDriver());
    }

    public FormInput getMinPageSizeBox() {
        return new FormInput(findElement(By.name("min_page_size")), getDriver());
    }

    public FormInput getPageTimeoutBox() {
        return new FormInput(findElement(By.name("page_timeout")), getDriver());
    }

    private WebElement scheduleButton(String time){
        return findElement(By.xpath("//button[text()='" + time + "']"));
    }

    public WebElement hoursButton(){
        return scheduleButton("Hours");
    }

    public WebElement daysButton(){
        return scheduleButton("Days");
    }

    public WebElement weeksButton(){
        return scheduleButton("Weeks");
    }

    public String scheduleString(){
        return findElement(By.cssSelector("label.ng-scope.m-t")).getText();
    }

    public List<WebElement> getAllButtons() {
        List<WebElement> buttons = new ArrayList<>();
        buttons.add(hoursButton());
        buttons.add(daysButton());
        buttons.add(weeksButton());
        return buttons;
    }

    public WebElement credentialsConfigurations() {
        return findElement(By.id("CredentialsConfigurationPropsHeader"));
    }

    public WebElement addCredentialsCheckbox() {
        return credentialsConfigurations().findElement(By.tagName("ins"));
    }

    public FormInput urlRegexBox(){
        return new FormInput(findElement(By.name("form_url_regex")), getDriver());
    }

    public FormInput loginFieldBox(){
        return new FormInput(findElement(By.name("login_field_value")), getDriver());
    }

    public FormInput passwordFieldBox(){
        return new FormInput(findElement(By.name("password_field_value")), getDriver());
    }

    public FormInput submitButtonBox(){
        return new FormInput(findElement(By.name("submit_selector")), getDriver());
    }

    public FormInput loginUsernameBox(){
        return new FormInput(findElement(By.name("login_value")), getDriver());
    }

    public FormInput loginPasswordBox(){
        return new FormInput(findElement(By.name("password_value")), getDriver());
    }

    public FormInput notificationEmailBox(){
        return new FormInput(findElement(By.name("notification_email")), getDriver());
    }
}

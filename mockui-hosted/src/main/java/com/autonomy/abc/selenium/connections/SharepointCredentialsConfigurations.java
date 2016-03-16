package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class SharepointCredentialsConfigurations extends AppElement {
    public SharepointCredentialsConfigurations(WebDriver driver) {
        super(ElementUtil.ancestor(driver.findElement(By.id("CredentialsConfigurationPropsHeader")), 1), driver);
    }

    public FormInput userNameInput(){
        return new FormInput(findElement(By.name("username")), getDriver());
    }

    public FormInput passwordInput(){
        return new FormInput(findElement(By.name("password")), getDriver());
    }

    public FormInput notificationEmailInput(){
        return new FormInput(findElement(By.name("notification_email")), getDriver());
    }

    public WebElement onlineCheckbox(){
        return findElement(By.className("iCheck-helper"));
    }

    public void selectURLType(URLType urlType){
        Select dropdown = new Select(findElement(By.name("urlType")));
        dropdown.selectByVisibleText(urlType.getDropdownText());
    }

    public enum URLType{
        SITE("Site collection"),
        PERSONAL("Personal site collection"),
        WEB_APP("Web application");

        private final String dropdown;

        URLType(String dropdown){
            this.dropdown = dropdown;
        }

        String getDropdownText(){
            return dropdown;
        }
    }
}

package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class DropboxCredentialsConfigurations extends AppElement {
    public DropboxCredentialsConfigurations(WebDriver driver) {
        super(ElementUtil.ancestor(driver.findElement(By.id("CredentialsConfigurationPropsHeader")), 1), driver);
    }

    public WebElement fullDropboxAccess(){
        return findElement(By.className("iCheck-helper"));
    }

    public FormInput applicationKeyInput(){
        return new FormInput(findElement(By.name("app_key")), getDriver());
    }

    public FormInput accessTokenInput(){
        return new FormInput(findElement(By.name("access_token")), getDriver());
    }

    public FormInput notificationEmailInput(){
        return new FormInput(findElement(By.name("notification_email")), getDriver());
    }
}

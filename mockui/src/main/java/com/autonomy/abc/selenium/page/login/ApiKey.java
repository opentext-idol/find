package com.autonomy.abc.selenium.page.login;

import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.element.ModalView;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ApiKey implements AuthProvider {
    private String key;

    public ApiKey(String apiKey) {
        key = apiKey;
    }

    private WebElement getLoginModalOpenButton(WebDriver driver) {
        return driver.findElement(By.linkText("APIKey"));
    }

    private WebElement getInputBox(WebDriver driver) {
        return ModalView.getVisibleModalView(driver).findElement(By.className("js-apikey-input"));
    }

    private WebElement getSubmitButton(WebDriver driver) {
        return ModalView.getVisibleModalView(driver).findElement(By.id("apikey_submit"));
    }

    public void login(AppElement loginPage) {
        WebDriver driver = loginPage.getDriver();
        /* Clicking APIKey Button doesn't always open the modal first time. The loop below will retry the button if the modal doesn't open */
//        for (int i=0; i<10; i++) {
            getLoginModalOpenButton(driver).click();
//            if (loginPage.isModalShowing()) {
//                break;
//            }
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
        getInputBox(driver).sendKeys(key);
        getSubmitButton(driver).click();
    }
}

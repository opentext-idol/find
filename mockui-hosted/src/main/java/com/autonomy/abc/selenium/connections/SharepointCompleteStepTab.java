package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.icma.ICMAPageBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SharepointCompleteStepTab extends ICMAPageBase {
    public SharepointCompleteStepTab(WebDriver driver) {
        super(driver);
    }

    public WebElement downloadAgentButton(){
        return getDriver().findElement(By.xpath("//button[text()='Download agent']"));
    }

    public WebElement apiKey(){
        return getDriver().findElement(By.cssSelector(".form-control.ellipsis"));
    }

    public WebElement generateAPIKeyButton(){
        return getDriver().findElement(By.xpath("//a[text()='GENERATE APIKEY']"));
    }

    public WebElement modalCancel(){
        return getDriver().findElement(By.xpath("//button[text()='Cancel']"));
    }

    public WebElement agentInformationButton(){
        return getDriver().findElement(By.className("hp-help"));
    }
}

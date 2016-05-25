package com.autonomy.abc.selenium.find;


import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

import static com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER;

public class DatabaseFilterNode extends FilterNode{

    private WebElement container;

    DatabaseFilterNode(WebElement element, WebDriver webDriver){
        super(element,webDriver);
        container = element;
    }

    public List<WebElement> getChildren(){
        return container.findElements(By.xpath(("//span[contains(@class,'database-name')]")));
    }

    @Override
    public List<String> getChildNames(){
        return ElementUtil.getTexts(getChildren());
    }
}

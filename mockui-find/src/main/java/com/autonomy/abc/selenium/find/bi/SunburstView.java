package com.autonomy.abc.selenium.find.bi;


import com.hp.autonomy.frontend.selenium.util.DriverUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;


public class SunburstView extends ParametricFieldView {
    private static final int VISIBLE_SEGMENTS = 20;

    public SunburstView(final WebDriver driver) {
        super(driver);
    }

    //Display
    public boolean sunburstVisible(){
        return findElement(By.cssSelector(".sunburst svg")).isDisplayed();
    }

    public List<WebElement> findSunburstSegments(){
        return findElements(By.cssSelector(".sunburst path:not([fill='#f0f0f0']):not([fill='#ffffff'])"));
    }

    public int numberOfSunburstSegments(){
        return findSunburstSegments().size();
    }

    public void waitForSunburst(){
        new WebDriverWait(getDriver(),15).until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".parametric-loading")));
    }

    public String getSunburstCentreName(){
        return findElement(By.className("sunburst-sector-name")).getText();
    }

    private boolean sunburstCentreHasText(){
        return !findElements(By.className("sunburst-sector-name")).isEmpty();
    }

    public WebElement getIthSunburstSegment(final int i){
        final List<WebElement> actualSegments = findSunburstSegments();
        return actualSegments.get(i);
    }

    public void hoverOverTooFewToDisplaySegment(){
        DriverUtil.hover(getDriver(),findElement(By.cssSelector("svg > path[fill='#f0f0f0']")));
    }

    public boolean greySunburstAreaExists(){
        return !findElements(By.cssSelector("svg > path[fill='#f0f0f0']")).isEmpty();
    }

    public String hoverOnSegmentGetCentre(final int i){
        //DriverUtil.hover(getDriver(),getIthSunburstSegment(i));
        segmentHover(getIthSunburstSegment(i));
        return getSunburstCentreName();
    }

    public void segmentHover(final WebElement segment){
        DriverUtil.hover(getDriver(),segment);
        if(!sunburstCentreHasText()|| (sunburstCentreHasText() && getSunburstCentreName().equals("Parametric Distribution"))){
            specialHover(segment);
        }
    }

    private void specialHover(final WebElement segment){
        final Dimension dimensions = segment.getSize();

        hoveringOffSide(segment,(dimensions.getWidth()/4)*3, dimensions.getHeight()/2);
        if(!sunburstCentreHasText()|| (sunburstCentreHasText() && getSunburstCentreName().equals("Parametric Distribution"))){
            hoveringOffSide(segment, dimensions.getWidth()/4, dimensions.getHeight()/2);
        }
    }

    //Should go in DriverUtils
    private void hoveringOffSide(final WebElement element, final int xOffSet, final int yOffSet){
        final Actions builder = new Actions(getDriver());
        builder.moveToElement(element,xOffSet,yOffSet);
        final Action hover = builder.build();
        hover.perform();
    }
}

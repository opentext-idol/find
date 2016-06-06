package com.autonomy.abc.selenium.find;


import com.autonomy.abc.selenium.find.filters.FindParametricCheckbox;
import com.hp.autonomy.frontend.selenium.util.DriverUtil;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;


public class FindResultsSunburst extends FindResultsPage{
    private static final int VISIBLE_SEGMENTS = 20;

    public FindResultsSunburst(WebDriver driver) {
        super(driver);
    }

    //Navigation
    public void goToSunburst(){
       findElement(By.cssSelector("[data-tab-id='sunburst']")).click();
        new WebDriverWait(getDriver(),15).until(ExpectedConditions.visibilityOf(findElement(By.cssSelector(".sunburst"))));
    }

    //Display
    public boolean sunburstVisible(){
        return findElement(By.cssSelector(".sunburst svg")).isDisplayed();
    }

    public List<WebElement> findSunburstSegments(){
        return findElements(By.cssSelector("path:not([fill='#f0f0f0']):not([fill='#ffffff'])"));
    }

    public int numberOfSunburstSegments(){
        return findSunburstSegments().size();
    }

    public void waitForSunburst(){
        new WebDriverWait(getDriver(),15).until(ExpectedConditions.invisibilityOfElementLocated(By.className("view-server-loading-indicator")));
        }

    public String getSunburstCentreName(){
        return findElement(By.className("sunburst-sector-name")).getText();}

    private boolean sunburstCentreHasText(){
        return findElements(By.className("sunburst-sector-name")).size()>0;
    }

    public WebElement getIthSunburstSegment(int i){
        List<WebElement> actualSegments = findSunburstSegments();
        return actualSegments.get(i);
    }

    public void hoverOverTooFewToDisplaySegment(){
        DriverUtil.hover(getDriver(),findElement(By.cssSelector("svg > path[fill='#f0f0f0']")));
    }

    public boolean greySunburstAreaExists(){
        return findElements(By.cssSelector("svg > path[fill='#f0f0f0']")).size()>0;
    }

    public String hoverOnSegmentGetCentre(int i){
        DriverUtil.hover(getDriver(),getIthSunburstSegment(i));
        return getSunburstCentreName();
    }

    public void segmentHover(WebElement segment){
        DriverUtil.hover(getDriver(),segment);
        if(!sunburstCentreHasText()|| (sunburstCentreHasText() && getSunburstCentreName().equals("Parametric Distribution"))){
            specialHover(segment);
        }
    }

    private void specialHover(WebElement segment){
        Dimension dimensions = segment.getSize();

        hoveringOffSide(segment,(dimensions.getWidth()/4)*3, dimensions.getHeight()/2);
        if(!sunburstCentreHasText()|| (sunburstCentreHasText() && getSunburstCentreName().equals("Parametric Distribution"))){
            hoveringOffSide(segment,(dimensions.getWidth()/4), dimensions.getHeight()/2);
        }
    }

    private void hoveringOffSide(WebElement element, int xOffSet,int yOffSet){
        Actions builder = new Actions(getDriver());
        builder.moveToElement(element,xOffSet,yOffSet);
        Action hover = builder.build();
        hover.perform();
    }

    //Parametric Filtering
    public String getSelectedFieldName(int i){
        return nthParametricFilter(i).getText();
    }

    private WebElement nthParametricFilter(int i){
        return findElement(By.cssSelector(".parametric-selections span:nth-child("+i+")"));
    }

    public boolean parametricSelectionDropdownsExist(){return findElement(By.cssSelector(".parametric-selections span")).isDisplayed();}

    public ChosenDrop parametricSelectionDropdown(int i){
        return new ChosenDrop(nthParametricFilter(i),getDriver());
    }

    public List<String> getParametricDropdownItems(int i){
        ChosenDrop dropdown = parametricSelectionDropdown(i);
        return ElementUtil.getTexts(dropdown.getItems());
    }

    /**
     * Determines which values for a parametric field are significant
     * enough to be displayed in sunburst
     * @param checkboxes some iterable of parametric values
     * @return the significant values
     */
    public static List<String> expectedParametricValues(Iterable<FindParametricCheckbox> checkboxes) {
        final List<String> expected = new ArrayList<>();

        int totalResults = 0;
        for (FindParametricCheckbox checkbox : checkboxes) {
            totalResults += checkbox.getResultsCount();
        }

        for (FindParametricCheckbox checkbox : checkboxes) {
            int thisCount = checkbox.getResultsCount();
            if (expected.size() < VISIBLE_SEGMENTS || isBigEnough(thisCount, totalResults)) {
                expected.add(checkbox.getName());
            } else {
                break;
            }
        }
        return expected;
    }

    private static boolean isBigEnough(int thisCount, int totalResults) {
        return ((double) thisCount)/totalResults >= 0.05;
    }


}

package com.autonomy.abc.selenium.find.bi;


import com.autonomy.abc.selenium.find.Container;
import com.google.common.base.Function;
import com.hp.autonomy.frontend.selenium.util.DriverUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;


public class SunburstView extends ParametricFieldView {
    private static final int SUNBURST_LOAD_TIMEOUT = 60;

    public SunburstView(final WebDriver driver) {
        super(driver, By.xpath(".//*[contains(@class,'parametric-content') and contains(@class,'sunburst')]/../.."));
    }

    //Display
    public boolean sunburstVisible() {
        return findElement(By.cssSelector(".sunburst svg")).isDisplayed();
    }

    public List<WebElement> findSunburstSegments() {
        return Container.currentTabContents(getDriver())
                .findElements(By.cssSelector(".sunburst path:not([fill='#f0f0f0']):not([fill='#ffffff']):not([fill='white'])"));
    }

    public int numberOfSunburstSegments() {
        return findSunburstSegments().size();
    }

    public void waitForSunburst() {
        new WebDriverWait(getDriver(), SUNBURST_LOAD_TIMEOUT).withMessage("waiting for sunburst or sunburst message")
                .until((Function<? super WebDriver, Boolean>) driver -> {
                    final WebElement sunburstView = driver.findElement(By.xpath(".//*[contains(@class,'parametric-content') and contains(@class,'sunburst')]/../.."));
                    return (!sunburstView.findElements(By.cssSelector("svg")).isEmpty()
                            || sunburstView.findElement(By.cssSelector(".parametric-view-message")).isDisplayed())
                            && !sunburstView.findElement(By.cssSelector(".parametric-loading")).isDisplayed();
                });
    }

    //TODO: IE specific stale element exception: may be IE driver related or bug.
    public String getSunburstCentreName() {
        return sunburstCentre().getText();
    }

    private WebElement sunburstCentre() {
        return findElement(By.className("sunburst-sector-name"));
    }

    private boolean sunburstCentreHasText() {
        return !findElements(By.className("sunburst-sector-name")).isEmpty();
    }

    public WebElement getIthSunburstSegment(final int i) {
        final List<WebElement> actualSegments = findSunburstSegments();
        return actualSegments.get(i);
    }

    public void hoverOverTooFewToDisplaySegment() {
        final WebElement areaWithGrey = findElement(By.cssSelector("svg > path[fill='#f0f0f0']"));
        final Dimension dimensions = areaWithGrey.getSize();

        DriverUtil.hoveringOffSide(areaWithGrey, new Point(dimensions.getWidth() / 6, dimensions.getHeight() / 2), getDriver());
    }

    public boolean greySunburstAreaExists() {
        return !findElements(By.cssSelector("svg > path[fill='#f0f0f0']")).isEmpty();
    }

    public String hoverOnSegmentGetCentre(final int i) {
        segmentHover(getIthSunburstSegment(i));
        return getSunburstCentreName();
    }

    public void segmentHover(final WebElement segment) {
        DriverUtil.hover(getDriver(), segment);

        if (!sunburstCentreHasText() || sunburstCentreHasText() && "Parametric Distribution".equals(getSunburstCentreName())) {
            specialHover(segment);
        }
    }

    private void specialHover(final WebElement segment) {
        final Dimension dimensions = segment.getSize();

        DriverUtil.hoveringOffSide(segment, new Point(dimensions.getWidth() / 4 * 3, dimensions.getHeight() / 2), getDriver());
        if (!sunburstCentreHasText() || sunburstCentreHasText() && "Parametric Distribution".equals(getSunburstCentreName())) {
            DriverUtil.hoveringOffSide(segment, new Point(dimensions.getWidth() / 4, dimensions.getHeight() / 2), getDriver());
        }
    }
}

package com.autonomy.abc.find;


import com.autonomy.abc.base.FindTestBase;
import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.selenium.find.FindResultsTopicMap;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.FindTopNavBar;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.filters.FindParametricCheckbox;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.CommonMatchers.containsItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TopicMapITCase extends IdolFindTestBase {
    private IdolFindPage findPage;
    private FindResultsTopicMap results;
    private FindService findService;
    private FindTopNavBar navBar;

    public TopicMapITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        findPage = getElementFactory().getFindPage();
        results = getElementFactory().getTopicMap();
        findService = getApplication().findService();
        navBar = getElementFactory().getTopNavBar();
    }

    @Test
    public void testTopicMapTabShowsTopicMap() {
        findService.search("shambolic");
        results.goToTopicMap();
        verifyThat("Main results list hidden", results.mainResultsContainerHidden());
        verifyThat("Topic map element displayed", results.topicMapVisible());
    }

    @Test
    public void testNumbersForMapInSliders() {
        findService.search("gove");
        results.goToTopicMap();

        slidingIncreasesNumber(results.numberTopicsSlider());
        slidingIncreasesNumber(results.relevanceVsClusteringSlider());

        results.hoverOnSlider(results.numberTopicsSlider());
        verifyThat("Number of topics tooltip number correct", results.numberinMap(results.numberTopicsSlider()), is(results.numberOfMapEntities()));
    }

    private void slidingIncreasesNumber(WebElement slider) {
        results.hoverOnSlider(slider);
        new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(results.sliderToolTip(slider)));
        verifyThat("Tooltip appears on hover", results.sliderToolTip(slider).isDisplayed());
        int firstNumber = results.numberinMap(slider);

        results.dragSlider(slider);
        results.hoverOnSlider(slider);
        assertThat("Tooltip reappears after dragging", results.sliderToolTip(slider).isDisplayed());

        verifyThat("New tooltip value bigger than old", results.numberinMap(slider), greaterThanOrEqualTo(firstNumber));
    }

    @Test
    public void testEveryMapEntityHasText() {
        findService.search("trouble");
        results.goToTopicMap();

        results.dragSlider(results.numberTopicsSlider());

        results.waitForMapLoaded();
        results.hoverOnSlider(results.numberTopicsSlider());
        int numberEntities = results.numberinMap(results.numberTopicsSlider());

        List<WebElement> textElements = results.mapEntityTextElements();
        verifyThat("Same number of text elements as map pieces", textElements.size(), is(numberEntities));

        for (WebElement textElement : textElements) {
            verifyThat("Text element not empty", textElement.getText(), not(""));
        }

    }

    @Test
    public void testApplyingFiltersToMap() {
        String searchTerm = "European Union";
        findService.search(searchTerm);
        results.goToTopicMap();

        FindParametricCheckbox filter = getElementFactory().getFilterPanel().checkboxForParametricValue(0, 0);
        String filterName = filter.getName();
        filter.check();

        results.waitForReload();
        verifyThat("The correct filter label has appeared", findPage.getFilterLabels(), hasItem(equalToIgnoringCase(filterName)));
        verifyThat("Search term still " + searchTerm, navBar.getSearchBoxTerm(), is(searchTerm));
    }

    @Test
    public void testClickingOnMapEntities(){
        findService.search("rubbish");
        results.goToTopicMap();

        results.waitForMapLoaded();

        try{
            Thread.sleep(1000);
        } catch(InterruptedException e){}

        List<String> addedConcepts = results.clickEntitiesAndAddText(3);
        verifyThat("All "+addedConcepts.size()+" added concept terms added to search",relatedConceptsWithoutSpaces(),containsItems(addedConcepts));
    }

    private List<String> relatedConceptsWithoutSpaces(){
        List<String> termsNoSpaces = new ArrayList<>();
        for(String term: navBar.getAlsoSearchingForTerms()){
            termsNoSpaces.add(term.replace(" ",""));
        }
        return termsNoSpaces;
    }
}

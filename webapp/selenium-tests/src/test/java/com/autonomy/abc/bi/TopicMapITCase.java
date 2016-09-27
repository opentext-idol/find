package com.autonomy.abc.bi;


import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.base.Role;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.application.BIIdolFindElementFactory;
import com.autonomy.abc.selenium.find.application.UserRole;
import com.autonomy.abc.selenium.find.bi.TopicMapView;
import com.autonomy.abc.selenium.find.concepts.ConceptsPanel;
import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.find.filters.FindParametricFilter;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.element.Slider;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.CommonMatchers.containsItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Role(UserRole.BIFHI)
public class TopicMapITCase extends IdolFindTestBase {
    private static final Pattern SPACE_PATTERN = Pattern.compile(" ", Pattern.LITERAL);

    private IdolFindPage findPage;
    private TopicMapView results;
    private FindService findService;
    private ConceptsPanel conceptsPanel;

    public TopicMapITCase(final TestConfig config) {
        super(config);
    }

    @Override
    public BIIdolFindElementFactory getElementFactory() {
        return (BIIdolFindElementFactory) super.getElementFactory();
    }

    @Before
    public void setUp() {
        final BIIdolFindElementFactory elementFactory = getElementFactory();
        findPage = elementFactory.getFindPage();
        results = elementFactory.getTopicMap();
        findService = getApplication().findService();
        conceptsPanel = elementFactory.getConceptsPanel();
    }

    @Test
    public void testTopicMapTabShowsTopicMap() {
        findService.search("shambolic");
        verifyThat("Topic map element displayed", results.topicMapVisible());
    }

    @Test
    public void testNumbersForMapInSliders() {
        findService.search("gove");
        slidingIncreasesNumber(results.speedVsAccuracySlider());
    }

    private void slidingIncreasesNumber(final Slider slider) {
        slider.hover();
        new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(slider.tooltip()));
        verifyThat("Tooltip appears on hover", slider.tooltip().isDisplayed());
        final int firstNumber = slider.getValue();

        slider.dragBy(100);
        slider.hover();
        assertThat("Tooltip reappears after dragging", slider.tooltip().isDisplayed());

        verifyThat("New tooltip value bigger than old", slider.getValue(), greaterThanOrEqualTo(firstNumber));
    }

    @Test
    public void testEveryMapEntityHasText() {
        findService.search("trouble");

        results.speedVsAccuracySlider().dragBy(100);
        Waits.loadOrFadeWait();
        results.waitForMapLoaded();

        results.speedVsAccuracySlider().hover();
        final int numberEntities = results.numberOfMapEntities();

        final List<WebElement> textElements = results.mapEntityTextElements();
        verifyThat("Same number of text elements as map pieces", textElements.size(), is(numberEntities));

        for (final WebElement textElement : textElements) {
            verifyThat("Text element not empty", textElement.getText(), not(""));
        }
    }

    @Test
    public void testApplyingFiltersToMap() {
        final String searchTerm = "European Union";
        findService.search(searchTerm);

        //checks first parametric filter of first parametric filter type
        final FilterPanel filters = getElementFactory().getFilterPanel();
        final int index = filters.nonZeroParamFieldContainer(0);
        filters.parametricField(index).expand();
        final FindParametricFilter filter = filters.checkboxForParametricValue(index, 0);
        final String filterName = filter.getName();
        filter.check();

        results.waitForMapLoaded();
        verifyThat("The correct filter label has appeared", findPage.filterLabelsText(), hasItem(containsString(filterName)));
    }

    @Test
    public void testClickingOnMapEntities() {
        findService.search("rubbish");
        results.waitForMapLoaded();
        Waits.loadOrFadeWait();

        final List<String> clusterNames = results.returnParentEntityNames();
        final List<String> addedConcepts = new ArrayList<>();
        addedConcepts.add(results.clickChildEntityAndAddText(clusterNames.size()));
        results.waitForMapLoaded();
        addedConcepts.add(results.clickChildEntityAndAddText(results.returnParentEntityNames().size()));

        LOGGER.info("This is currently brittle and frequently breaks.");
        verifyThat("All " + addedConcepts.size() + " added concept terms added to search", selectedConcepts(), containsItems(addedConcepts));
    }

    private String stripSpaces(final CharSequence term) {
        return SPACE_PATTERN.matcher(term).replaceAll(Matcher.quoteReplacement(""));
    }

    private List<String> selectedConcepts() {
        return conceptsPanel.selectedConceptHeaders().stream()
                .map(this::stripSpaces)
                .collect(Collectors.toList());
    }

}

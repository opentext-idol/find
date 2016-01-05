package com.autonomy.abc.connections;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.indexes.CreateNewIndexPage;
import com.autonomy.abc.selenium.util.ElementUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static com.autonomy.abc.matchers.ElementMatchers.hasClass;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.openqa.selenium.lift.Matchers.displayed;

public class IndexWizardITCase extends HostedTestBase {

    private CreateNewIndexPage createNewIndexPage;
    private FormInput indexNameInput;
    private FormInput displayNameInput;

    public IndexWizardITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    @Before
    public void setUp(){
        body.getSideNavBar().switchPage(NavBarTabId.INDEXES);

        getElementFactory().getIndexesPage().newIndexButton().click();
        body = getBody();

        createNewIndexPage = getElementFactory().getCreateNewIndexPage();
        indexNameInput = createNewIndexPage.indexNameInput();
        displayNameInput = createNewIndexPage.displayNameInput();
    }

    @Test
    //CSA-949 - test the input validator which supports only A-Za-Z0-9, space and underscore characters - shouldn't be valid
    public void testIndexDisplayNameValidatorsFail(){
        indexNameInput.setValue("name");
        displayNameInput.setValue("displayName #$%");

        String error = "Please enter a valid name that contains only alphanumeric characters";
        verifyThat(displayNameInput.formGroup(), hasClass("has-error"));
        WebElement errorMessage = displayNameInput.errorMessage();
        verifyThat(errorMessage, displayed());
        verifyThat(errorMessage, containsText(error));
    }

    @Test
    //CSA-949 - test the input validator which supports only A-Za-Z0-9, space and underscore characters - should be valid
    public void testIndexDisplayNameValidatorsPass(){
        indexNameInput.setValue("name");
        displayNameInput.setValue("displayName 7894");

        WebElement errorMessage = null;
        try {
            errorMessage = displayNameInput.errorMessage();
        } catch (NoSuchElementException e) {
            /* expected behaviour */
        } finally {
            verifyThat(errorMessage, nullValue());
        }
        verifyThat(displayNameInput.formGroup(), not(hasClass("has-error")));

    }

    @Test
    //CSA-949 - test that the index meta-data with the index display name is set properly according to the summary step
    public void testIndexDisplayNameOnSummary(){
        String name = "name";
        String displayName = "displayName 7894";

        indexNameInput.setValue(name);
        displayNameInput.setValue(displayName);

        verifyThat(indexNameInput.formGroup(), not(hasClass("has-error")));
        verifyThat(displayNameInput.formGroup(), not(hasClass("has-error")));

        createNewIndexPage.nextButton().click();
        createNewIndexPage.nextButton().click();

        WebElement summaryStepIndexDescriptionLabel = createNewIndexPage.summaryStepIndexDescriptionLabel();
        String expectedSummary = "A new index named "+name+" with "+displayName+" as display name (standard flavor) will be created";
        verifyThat(summaryStepIndexDescriptionLabel, containsText(expectedSummary));
    }

    @Test
    //CSA-1616
    public void testUppercaseFieldNames() {
        indexNameInput.setValue("name");

        createNewIndexPage.nextButton().click();

        final List<String> capitals = new ArrayList<String>() {{
            add("London");
            add("Paris");
            add("Washington");
        }};

        List<String> lowercase = new ArrayList<String>() {{
            for(String capital : capitals){
                add(capital.toLowerCase());
            }
        }};

        createNewIndexPage.inputIndexFields(capitals);

        WebElement errorMessage = configErrorMessage(createNewIndexPage.advancedIndexFields());
        String error = "field names can contain only lowercase alphanumeric characters";

        verifyThat(errorMessage, displayed());
        verifyThat(errorMessage, containsText(error));

        createNewIndexPage.advancedIndexFields().clear();
        createNewIndexPage.inputIndexFields(lowercase);

        verifyThat(errorMessage, not(displayed()));

        createNewIndexPage.inputParametricFields(capitals);

        errorMessage = configErrorMessage(createNewIndexPage.advancedParametricFields());

        verifyThat(errorMessage, displayed());
        verifyThat(errorMessage, containsText(error));

        createNewIndexPage.advancedParametricFields().clear();
        createNewIndexPage.inputParametricFields(lowercase);

        verifyThat(errorMessage, not(displayed()));
    }

    private WebElement configErrorMessage(WebElement element){
        return ElementUtil.ancestor(element,1).findElement(By.tagName("p"));
    }

    @After
    public void tearDown(){}
}

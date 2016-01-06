package com.autonomy.abc.connections;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.indexes.CreateNewIndexPage;
import com.autonomy.abc.selenium.util.Errors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static com.autonomy.abc.matchers.ElementMatchers.hasClass;
import static org.hamcrest.Matchers.*;

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

        verifyThat(displayNameInput.formGroup(), hasClass("has-error"));
        verifyThat(displayNameInput.getErrorMessage(), containsString(Errors.Index.DISPLAY_NAME));
    }

    @Test
    //CSA-949 - test the input validator which supports only A-Za-Z0-9, space and underscore characters - should be valid
    public void testIndexDisplayNameValidatorsPass(){
        indexNameInput.setValue("name");
        displayNameInput.setValue("displayName 7894");

        verifyThat(displayNameInput.formGroup(), not(hasClass("has-error")));
        verifyThat(displayNameInput.getErrorMessage(), nullValue());
    }

    @Test
    //CSA-949 - test that the index meta-data with the index display name is set properly according to the summary step
    public void testIndexDisplayNameOnSummary(){
        String name = "name";
        String displayName = "displayName 7894";

        indexNameInput.setValue(name);
        displayNameInput.setValue(displayName);

        verifyThat(indexNameInput.getErrorMessage(), nullValue());
        verifyThat(displayNameInput.getErrorMessage(), nullValue());

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

        createNewIndexPage.setIndexFields(capitals);
        FormInput input = createNewIndexPage.indexFieldsInput();
        verifyThat(input.getErrorMessage(), containsString(Errors.Index.FIELD_NAMES));

        createNewIndexPage.setIndexFields(lowercase);
        verifyThat(input.getErrorMessage(), nullValue());

        createNewIndexPage.setParametricFields(capitals);
        input = createNewIndexPage.parametricFieldsInput();
        verifyThat(input.getErrorMessage(), containsString(Errors.Index.FIELD_NAMES));

        createNewIndexPage.setParametricFields(lowercase);
        verifyThat(input.getErrorMessage(), nullValue());
    }

    @After
    public void tearDown(){}
}

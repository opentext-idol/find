package com.autonomy.abc.connections;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.indexes.CreateNewIndexPage;
import com.autonomy.abc.selenium.page.indexes.wizard.IndexConfigStepTab;
import com.autonomy.abc.selenium.page.indexes.wizard.IndexNameWizardStepTab;
import com.autonomy.abc.selenium.util.Errors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static com.autonomy.abc.matchers.ElementMatchers.hasClass;
import static org.hamcrest.Matchers.*;

public class IndexWizardITCase extends HostedTestBase {

    private CreateNewIndexPage createNewIndexPage;

    public IndexWizardITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp(){
        body.getSideNavBar().switchPage(NavBarTabId.INDEXES);

        getElementFactory().getIndexesPage().newIndexButton().click();
        body = getBody();

        createNewIndexPage = getElementFactory().getCreateNewIndexPage();
    }

    @Test
    //CSA-949 - test the input validator which supports only A-Za-Z0-9, space and underscore characters - shouldn't be valid
    public void testIndexDisplayNameValidatorsFail(){
        Index index = new Index("name", "displayName #$%");

        index.makeWizard(createNewIndexPage).getCurrentStep().apply();
        FormInput displayNameInput = createNewIndexPage.getIndexNameWizardStepTab().displayNameInput();

        verifyThat(displayNameInput.formGroup(), hasClass("has-error"));
        verifyThat(displayNameInput.getErrorMessage(), containsString(Errors.Index.DISPLAY_NAME));
    }

    @Test
    //CSA-949 - test the input validator which supports only A-Za-Z0-9, space and underscore characters - should be valid
    public void testIndexDisplayNameValidatorsPass(){
        Index index = new Index("name", "displayName 7894");

        index.makeWizard(createNewIndexPage).getCurrentStep().apply();
        FormInput displayNameInput = createNewIndexPage.getIndexNameWizardStepTab().displayNameInput();

        verifyThat(displayNameInput.formGroup(), not(hasClass("has-error")));
        verifyThat(displayNameInput.getErrorMessage(), nullValue());
    }

    @Test
    //CSA-949 - test that the index meta-data with the index display name is set properly according to the summary step
    public void testIndexDisplayNameOnSummary(){
        Index index = new Index("name", "displayName 7894");

        Wizard wizard = index.makeWizard(createNewIndexPage);
        IndexNameWizardStepTab indexNameWizardStepTab = createNewIndexPage.getIndexNameWizardStepTab();

        wizard.getCurrentStep().apply();
        verifyThat(indexNameWizardStepTab.indexNameInput().getErrorMessage(), nullValue());
        verifyThat(indexNameWizardStepTab.displayNameInput().getErrorMessage(), nullValue());

        wizard.next();
        wizard.next();

        String expectedSummary = "A new index named "+ index.getName() +" with " + index.getDisplayName() + " as display name (standard flavor) will be created";
        verifyThat(createNewIndexPage.getIndexSummaryStepTab().indexDescriptionLabel(), containsText(expectedSummary));
    }

    @Test
    //CSA-2042 - test index name with character length grater than 100
    public void testIndexNameFieldMaxCharacterLength(){
        Index index = new Index("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz");
        index.makeWizard(createNewIndexPage).getCurrentStep().apply();
        FormInput indexNameInput = createNewIndexPage.getIndexNameWizardStepTab().indexNameInput();

        verifyThat(indexNameInput.formGroup(), hasClass("has-error"));
        verifyThat(indexNameInput.getErrorMessage(), containsString(Errors.Index.MAX_CHAR_LENGTH));
    }

    @Test
    //CSA-2042 - test index display name with character length grater than 100
    public void testDisplayNameFieldMaxCharacterLength(){
        Index index = new Index("validname","abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz");
        index.makeWizard(createNewIndexPage).getCurrentStep().apply();
        FormInput displayNameInput = createNewIndexPage.getIndexNameWizardStepTab().displayNameInput();

        verifyThat(displayNameInput.formGroup(), hasClass("has-error"));
        verifyThat(displayNameInput.getErrorMessage(), containsString(Errors.Index.MAX_CHAR_LENGTH));
    }

    @Test
    //CSA-1616
    public void testUppercaseFieldNames() {
        Index index = new Index("name");

        Wizard wizard = index.makeWizard(createNewIndexPage);

        wizard.getCurrentStep().apply();
        wizard.next();

        IndexConfigStepTab indexConfigStepTab = createNewIndexPage.getIndexConfigStepTab();

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

        indexConfigStepTab.setIndexFields(capitals);
        FormInput input = indexConfigStepTab.indexFieldsInput();
        verifyThat(input.getErrorMessage(), containsString(Errors.Index.FIELD_NAMES));

        indexConfigStepTab.setIndexFields(lowercase);
        verifyThat(input.getErrorMessage(), nullValue());

        indexConfigStepTab.setParametricFields(capitals);
        input = indexConfigStepTab.parametricFieldsInput();
        verifyThat(input.getErrorMessage(), containsString(Errors.Index.FIELD_NAMES));

        indexConfigStepTab.setParametricFields(lowercase);
        verifyThat(input.getErrorMessage(), nullValue());
    }

    @After
    public void tearDown(){}
}

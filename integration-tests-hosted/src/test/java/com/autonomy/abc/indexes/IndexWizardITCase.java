package com.autonomy.abc.indexes;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.KnownBug;
import com.autonomy.abc.framework.RelatedTo;
import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.indexes.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.autonomy.abc.framework.TestStateAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static com.autonomy.abc.matchers.ElementMatchers.hasClass;
import static com.autonomy.abc.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class IndexWizardITCase extends HostedTestBase {

    private CreateNewIndexPage createNewIndexPage;

    public IndexWizardITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp(){
        createNewIndexPage = getApplication().indexService().goToIndexWizard();
    }

    @Test
    @RelatedTo("CSA-949") // test the input validator which supports only A-Za-Z0-9, space and underscore characters - should be valid
    public void testIndexDisplayNameValidatorsPass(){
        Index index = new Index("name", "displayName 7894");

        new IndexWizard(index, createNewIndexPage).getCurrentStep().apply();
        FormInput displayNameInput = createNewIndexPage.getIndexNameWizardStepTab().displayNameInput();

        verifyThat(displayNameInput.formGroup(), not(hasClass("has-error")));
        verifyThat(displayNameInput.getErrorMessage(), nullValue());
    }

    @Test
    @RelatedTo("CSA-949") // test the input validator which supports only A-Za-Z0-9, space and underscore characters - shouldn't be valid
    public void testIndexDisplayNameValidatorsFail(){
        Index index = new Index("name", "displayName #$%");

        new IndexWizard(index, createNewIndexPage).getCurrentStep().apply();
        FormInput displayNameInput = createNewIndexPage.getIndexNameWizardStepTab().displayNameInput();

        verifyThat(displayNameInput.formGroup(), hasClass("has-error"));
        verifyThat(displayNameInput.getErrorMessage(), containsString(Errors.Index.DISPLAY_NAME));
    }

    @Test
    @RelatedTo("CSA-949") // test that the index meta-data with the index display name is set properly according to the summary step
    public void testIndexDisplayNameOnSummary(){
        Index index = new Index("name", "displayName 7894");

        Wizard wizard = new IndexWizard(index, createNewIndexPage);
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
    @KnownBug("CSA-2042")
    public void testIndexNameFieldMaxCharacterLength(){
        Index index = new Index("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz");
        new IndexWizard(index, createNewIndexPage).getCurrentStep().apply();
        FormInput indexNameInput = createNewIndexPage.getIndexNameWizardStepTab().indexNameInput();

        verifyThat(indexNameInput.formGroup(), hasClass("has-error"));
        verifyThat(indexNameInput.getErrorMessage(), containsString(Errors.Index.MAX_CHAR_LENGTH));
    }

    @Test
    @KnownBug("CSA-2042")
    public void testDisplayNameFieldMaxCharacterLength(){
        Index index = new Index("validname","abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz");
        new IndexWizard(index, createNewIndexPage).getCurrentStep().apply();
        FormInput displayNameInput = createNewIndexPage.getIndexNameWizardStepTab().displayNameInput();

        verifyThat(displayNameInput.formGroup(), hasClass("has-error"));
        verifyThat(displayNameInput.getErrorMessage(), containsString(Errors.Index.MAX_CHAR_LENGTH));
    }

    @Test
    @KnownBug("CSA-1616")
    public void testUppercaseFieldNames() {
        Index index = new Index("name");

        Wizard wizard = new IndexWizard(index, createNewIndexPage);

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
}

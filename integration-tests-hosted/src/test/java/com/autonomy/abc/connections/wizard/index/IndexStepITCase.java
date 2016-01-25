package com.autonomy.abc.connections.wizard.index;

import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.connections.wizard.ConnectorTypeStepBase;
import com.autonomy.abc.framework.KnownBug;
import com.autonomy.abc.framework.RelatedTo;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.page.connections.wizard.ConnectorIndexStepTab;
import com.autonomy.abc.selenium.page.connections.wizard.ConnectorType;
import com.autonomy.abc.selenium.page.connections.wizard.ConnectorTypeStepTab;
import com.autonomy.abc.selenium.util.Errors;
import com.autonomy.abc.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.hasClass;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.text.IsEmptyString.isEmptyOrNullString;

public class IndexStepITCase extends ConnectorTypeStepBase {
    public IndexStepITCase(TestConfig config) {
        super(config);
    }

    private ConnectorIndexStepTab connectorIndexStepTab;

    @Before
    public void navigateToStep(){
        ConnectorTypeStepTab connectorTypeStep = newConnectionPage.getConnectorTypeStep();
        FormInput connectorUrl = connectorTypeStep.connectorUrl();
        FormInput connectorName = connectorTypeStep.connectorName();
        selectConnectorType(ConnectorType.WEB);

        connectorUrl.setValue("http://www.foo.com");
        connectorName.setValue("foo");
        Waits.loadOrFadeWait();

        newConnectionPage.nextButton().click();
        Waits.loadOrFadeWait();

        newConnectionPage.getConnectorConfigStep();
        newConnectionPage.nextButton().click();
        Waits.loadOrFadeWait();

        connectorIndexStepTab = newConnectionPage.getIndexStep();

    }

    @Test
    public void testIndexNameValidatorsFail(){
        connectorIndexStepTab.setIndexName("name With UpperCase");

        String errorMessage = connectorIndexStepTab.indexNameInput().getErrorMessage();
        WebElement indexNameFormGroup = connectorIndexStepTab.indexNameInput().formGroup();
        verifyThat(indexNameFormGroup, hasClass("has-error"));
        verifyThat(errorMessage, is(Errors.Index.INDEX_NAME));
    }

    @Test
    @RelatedTo("CSA-949") // test the input validator which supports only A-Za-Z0-9, space and underscore characters - should be valid
    public void testIndexDisplayNameValidatorsPass() {
        connectorIndexStepTab.setIndexName("name");
        connectorIndexStepTab.setIndexDisplayName("displayName 7894");
        String errorMessage = connectorIndexStepTab.indexDisplayNameInput().getErrorMessage();
        verifyThat(errorMessage, isEmptyOrNullString());
        WebElement displayNameFormGroup = connectorIndexStepTab.indexDisplayNameInput().formGroup();

        verifyThat(displayNameFormGroup, not(hasClass("has-error")));

    }

    @Test
    @RelatedTo("CSA-949") // test the input validator which supports only A-Za-Z0-9, space and underscore characters - shouldn't be valid
    public void testIndexDisplayNameValidatorsFail(){
        connectorIndexStepTab.setIndexName("name");

        connectorIndexStepTab.setIndexDisplayName("displayName #$%");
        String errorMessage = connectorIndexStepTab.indexDisplayNameInput().getErrorMessage();
        WebElement displayNameFormGroup = connectorIndexStepTab.indexDisplayNameInput().formGroup();
        verifyThat(displayNameFormGroup, hasClass("has-error"));
        verifyThat(errorMessage, is(Errors.Index.DISPLAY_NAME));
    }

    @Test
    @KnownBug("CSA-2042")
    public void testIndexNameFieldMaxCharacterLength(){
        connectorIndexStepTab.setIndexName("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz");

        String errorMessage = connectorIndexStepTab.indexNameInput().getErrorMessage();
        WebElement indexNameFormGroup = connectorIndexStepTab.indexNameInput().formGroup();
        verifyThat(indexNameFormGroup, hasClass("has-error"));
        verifyThat(errorMessage, is(Errors.Index.MAX_CHAR_LENGTH));
    }

    @Test
    @KnownBug("CSA-2042")
    public void testDisplayNameFieldMaxCharacterLength(){
        connectorIndexStepTab.setIndexName("name");
        connectorIndexStepTab.setIndexDisplayName("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz");

        String errorMessage = connectorIndexStepTab.indexDisplayNameInput().getErrorMessage();
        WebElement displayNameFormGroup = connectorIndexStepTab.indexDisplayNameInput().formGroup();
        verifyThat(displayNameFormGroup, hasClass("has-error"));
        verifyThat(errorMessage, is(Errors.Index.MAX_CHAR_LENGTH));
    }
}

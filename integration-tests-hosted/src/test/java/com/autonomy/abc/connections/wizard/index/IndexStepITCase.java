package com.autonomy.abc.connections.wizard.index;

import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.connections.wizard.ConnectorTypeStepBase;
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
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static com.autonomy.abc.matchers.ElementMatchers.hasClass;
import static org.hamcrest.CoreMatchers.not;
import static org.openqa.selenium.lift.Matchers.displayed;

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
        connectorIndexStepTab.inputIndexName("name With UpperCase");

        String error = "Please enter a valid name that contains only lowercase alphanumeric characters";
        WebElement errorMessage = connectorIndexStepTab.configErrorMessage(connectorIndexStepTab.indexNameInputElement());
        WebElement displayNameFormGroup = connectorIndexStepTab.inputFormGroup(connectorIndexStepTab.indexNameInputElement());
        verifyThat(displayNameFormGroup, hasClass("has-error"));
        verifyThat(errorMessage, displayed());
        verifyThat(errorMessage, containsText(error));
    }

    @Test
    public void testIndexDisplayNameValidatorsFail(){
        connectorIndexStepTab.inputIndexName("name");

        connectorIndexStepTab.inputIndexDisplayName("displayName #$%");
        String error = "Please enter a valid name that contains only alphanumeric characters";
        WebElement errorMessage = connectorIndexStepTab.configErrorMessage(connectorIndexStepTab.indexDisplayNameInputElement());
        WebElement displayNameFormGroup = connectorIndexStepTab.inputFormGroup(connectorIndexStepTab.indexDisplayNameInputElement());
        verifyThat(displayNameFormGroup, hasClass("has-error"));
        verifyThat(errorMessage, displayed());
        verifyThat(errorMessage, containsText(error));
    }

    @Test
    //CSA-2042 - test index name validation with character length grater than 100
    public void testIndexNameFieldMaxCharacterLength(){
        connectorIndexStepTab.inputIndexName("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz");

        WebElement errorMessage = connectorIndexStepTab.getMaxLengthErrorMsg(connectorIndexStepTab.indexNameInputElement());
        WebElement indexNameFormGroup = connectorIndexStepTab.inputFormGroup(connectorIndexStepTab.indexNameInputElement());
        verifyThat(indexNameFormGroup, hasClass("has-error"));
        verifyThat(errorMessage, displayed());
        verifyThat(errorMessage, containsText(Errors.Index.MAX_CHAR_LENGTH));
    }

    @Test
    //CSA-2042 - test index name validation with character length grater than 100
    public void testDisplayNameFieldMaxCharacterLength(){
        connectorIndexStepTab.inputIndexName("name");
        connectorIndexStepTab.inputIndexDisplayName("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz");

        WebElement errorMessage = connectorIndexStepTab.getMaxLengthErrorMsg(connectorIndexStepTab.indexDisplayNameInputElement());
        WebElement displayNameFormGroup = connectorIndexStepTab.inputFormGroup(connectorIndexStepTab.indexDisplayNameInputElement());
        verifyThat(displayNameFormGroup, hasClass("has-error"));
        verifyThat(errorMessage, displayed());
        verifyThat(errorMessage, containsText(Errors.Index.MAX_CHAR_LENGTH));
    }

    @Test
    //CSA-949 - test the input validator which supports only A-Za-Z0-9, space and underscore characters - should be valid
    public void testIndexDisplayNameValidatorsPass(){
        connectorIndexStepTab.inputIndexName("name");
        connectorIndexStepTab.inputIndexDisplayName("displayName 7894");
        WebElement errorMessage = connectorIndexStepTab.configErrorMessage(connectorIndexStepTab.indexDisplayNameInputElement());
        verifyThat(errorMessage, not(displayed()));
        WebElement displayNameFormGroup = connectorIndexStepTab.inputFormGroup(connectorIndexStepTab.indexDisplayNameInputElement());

        verifyThat(displayNameFormGroup, not(hasClass("has-error")));

    }
}

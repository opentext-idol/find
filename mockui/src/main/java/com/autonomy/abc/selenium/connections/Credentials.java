package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.page.connections.wizard.ConnectorConfigStepTab;
import com.autonomy.abc.selenium.users.User;

public class Credentials {
    private enum CredentialsEnum {
        FACEBOOK("((http|https):\\/\\/)*(www)*\\.*facebook\\.com*\\/login.php\\?*([a-z]+=[a-z|%|0-9|A-Z|\\.]+&*)*", "email", "pass", "login");

        private final String regex;
        private final String loginSelector;
        private final String passSelector;
        private final String submitSelector;

        CredentialsEnum(String regex, String loginSelector, String passSelector, String submitSelector) {
            this.regex = regex;
            this.loginSelector = loginSelector;
            this.passSelector = passSelector;
            this.submitSelector = submitSelector;
        }
    }

    private final String username;
    private final String password;
    private final String email;
    private final CredentialsEnum credentialsEnum;

    public Credentials(String username, String password, String email){
        this.username = username;
        this.password = password;
        this.email = email;
        this.credentialsEnum = CredentialsEnum.FACEBOOK;
    }

    public void apply(ConnectorConfigStepTab connectorConfigStep) throws InterruptedException {
        connectorConfigStep.credentialsConfigurations().click();

        Thread.sleep(1000);

        connectorConfigStep.addCredentialsCheckbox().click();

        connectorConfigStep.urlRegexBox().setValue(credentialsEnum.regex);
        connectorConfigStep.loginFieldBox().setValue(credentialsEnum.loginSelector);
        connectorConfigStep.passwordFieldBox().setValue(credentialsEnum.passSelector);
        connectorConfigStep.submitButtonBox().setValue(credentialsEnum.submitSelector);
        connectorConfigStep.loginUsernameBox().setValue(username);
        connectorConfigStep.loginPasswordBox().setValue(password);
        connectorConfigStep.notificationEmailBox().setValue(email);
    }
}

package com.autonomy.abc.config;

import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.config.OPApplication;
import com.autonomy.abc.selenium.page.OPElementFactory;
import com.autonomy.abc.selenium.page.login.OPAccount;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openqa.selenium.Platform;

import java.net.MalformedURLException;

@Ignore
@RunWith(Parameterized.class)
public abstract class OPTestBase extends ABCTestBase {
	private String loginName;

    private OPElementFactory elementFactory;
    private OPApplication application;

	public OPTestBase(TestConfig config, String browser, ApplicationType type, Platform platform) {
		super(config, browser, type, platform);
	}

	@Override
	@Before
	public void baseSetUp() throws MalformedURLException {
		super.baseSetUp();
		this.application = (OPApplication) super.getApplication();
		this.elementFactory = (OPElementFactory) super.getElementFactory();
	}

	private void setLoginName(final String loginName) {
		this.loginName = loginName;
	}

	public String getLoginName() {
		return loginName;
	}

	public void abcOnPremiseLogin(final String userName, final String password) {
		this.loginName = userName;
		getElementFactory().getLoginPage().loginWith(new OPAccount(userName, password));
	}

	@Override
    public OPElementFactory getElementFactory() {
        return elementFactory;
    }

	@Override
    public OPApplication getApplication() {
        return application;
    }
}


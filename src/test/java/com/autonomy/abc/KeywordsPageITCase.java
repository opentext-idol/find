package com.autonomy.abc;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.page.KeywordsPage;
import org.junit.Before;
import org.openqa.selenium.Platform;

import java.net.MalformedURLException;

public class KeywordsPageITCase extends ABCTestBase {
	public KeywordsPageITCase(final TestConfig config, final String browser, final Platform platform) {
		super(config, browser, platform);
	}

	private KeywordsPage keywordsPage;

	@Before
	public void setUp() throws MalformedURLException {
		keywordsPage = body.getKeywordsPage();
	}


}
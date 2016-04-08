package com.autonomy.abc.base;

import com.autonomy.abc.selenium.application.SOElementFactory;
import com.autonomy.abc.selenium.application.SearchOptimizerApplication;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@Ignore
@RunWith(Parameterized.class)
public abstract class SOTestBase extends HybridAppTestBase<SearchOptimizerApplication<?>, SOElementFactory> {

	public SOTestBase(final TestConfig config) {
		super(config, SearchOptimizerApplication.ofType(config.getType()));
		setInitialUser(config.getDefaultUser());
	}
}

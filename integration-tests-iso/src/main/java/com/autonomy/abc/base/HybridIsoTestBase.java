package com.autonomy.abc.base;

import com.autonomy.abc.selenium.application.IsoApplication;
import com.autonomy.abc.selenium.application.IsoElementFactory;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@Ignore
@RunWith(Parameterized.class)
public abstract class HybridIsoTestBase extends HybridAppTestBase<IsoApplication<?>, IsoElementFactory> {

	public HybridIsoTestBase(final TestConfig config) {
		super(config, IsoApplication.ofType(config.getType()));
	}
}

package com.autonomy.abc.base;

import com.autonomy.abc.selenium.iso.IdolIsoApplication;
import com.autonomy.abc.selenium.iso.IdolIsoElementFactory;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Collections;

@Ignore
@RunWith(Parameterized.class)
public abstract class IdolIsoTestBase extends SOTestBase {
    public IdolIsoTestBase(TestConfig config) {
        super(config);
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> parameters() throws IOException {
        return parameters(Collections.singleton(ApplicationType.ON_PREM));
    }

    @Override
    public IdolIsoElementFactory getElementFactory() {
        return (IdolIsoElementFactory) super.getElementFactory();
    }

    @Override
    public IdolIsoApplication getApplication() {
        return (IdolIsoApplication) super.getApplication();
    }
}

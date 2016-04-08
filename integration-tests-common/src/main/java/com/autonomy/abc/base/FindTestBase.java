package com.autonomy.abc.base;

import com.autonomy.abc.config.SOConfigLocator;
import com.autonomy.abc.selenium.find.HsodFind;
import com.autonomy.abc.selenium.find.HsodFindElementFactory;
import com.hp.autonomy.frontend.selenium.base.TestParameterFactory;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.users.User;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;

@RunWith(Parameterized.class)
public abstract class FindTestBase extends HybridAppTestBase<HsodFind, HsodFindElementFactory> {
    protected FindTestBase(TestConfig config) {
        super(config, new HsodFind());
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> parameters() throws IOException {
        return new TestParameterFactory().create(new SOConfigLocator().getJsonConfig());
    }

    protected final User getCurrentUser() {
        return getApplication().loginService().getCurrentUser();
    }

}

package com.autonomy.abc.base;

import com.autonomy.abc.selenium.find.application.HodFind;
import com.autonomy.abc.selenium.find.application.HodFindElementFactory;
import com.autonomy.abc.selenium.find.application.UserRole;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Collections;

@RunWith(Parameterized.class)
public abstract class HodFindTestBase extends TestBase<HodFind<? extends HodFindElementFactory>, HodFindElementFactory> {
    protected HodFindTestBase(final TestConfig config) {
        super(config, HodFind.withRole(UserRole.activeRole()));
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> parameters() throws IOException {
        return parameters(Collections.singleton(ApplicationType.HOSTED));
    }
}

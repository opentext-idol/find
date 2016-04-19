package com.autonomy.abc.base;

import com.autonomy.abc.selenium.find.HsodFind;
import com.autonomy.abc.selenium.find.HsodFindElementFactory;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.users.User;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Collections;

@RunWith(Parameterized.class)
public abstract class FindTestBase extends HybridAppTestBase<HsodFind, HsodFindElementFactory> {
    protected FindTestBase(TestConfig config) {
        super(config, new HsodFind());
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> parameters() throws IOException {
        return HybridAppTestBase.parameters(Collections.singleton(ApplicationType.HOSTED));
    }

    protected final User getCurrentUser() {
        return getApplication().loginService().getCurrentUser();
    }

}

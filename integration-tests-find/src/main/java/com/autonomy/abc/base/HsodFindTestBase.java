package com.autonomy.abc.base;

import com.autonomy.abc.selenium.find.application.HsodFind;
import com.autonomy.abc.selenium.find.application.HsodFindElementFactory;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Collections;

@RunWith(Parameterized.class)
public class HsodFindTestBase extends HybridAppTestBase<HsodFind, HsodFindElementFactory> {
    protected HsodFindTestBase(TestConfig config) {
        super(config, new HsodFind());
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> parameters() throws IOException {
        return parameters(Collections.singleton(ApplicationType.HOSTED));
    }
}

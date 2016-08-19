package com.autonomy.abc.base;

import com.autonomy.abc.selenium.find.application.IdolFind;
import com.autonomy.abc.selenium.find.application.IdolFindElementFactory;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Collections;

@RunWith(Parameterized.class)
public class IdolFindTestBase extends HybridAppTestBase<IdolFind, IdolFindElementFactory> {
    protected IdolFindTestBase(final TestConfig config) {
        super(config, new IdolFind());
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> parameters() throws IOException {
        return parameters(Collections.singleton(ApplicationType.ON_PREM));
    }
}

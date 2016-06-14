package com.autonomy.abc.fixtures;

import com.autonomy.abc.base.HybridAppTestBase;
import com.autonomy.abc.selenium.application.IsoApplication;
import com.hp.autonomy.frontend.selenium.base.TearDown;

public abstract class IsoTearDownStrategyBase implements TearDown<HybridAppTestBase<? extends IsoApplication<?>, ?>> {
    @Override
    public final void tearDown(final HybridAppTestBase<? extends IsoApplication<?>, ?> test) {
        if (test.hasSetUp()) {
            cleanUpApp(test.getApplication());
        }
    }

    protected abstract void cleanUpApp(IsoApplication<?> app);
}

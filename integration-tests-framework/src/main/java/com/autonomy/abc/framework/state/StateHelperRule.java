package com.autonomy.abc.framework.state;

import com.autonomy.abc.base.SeleniumTest;
import com.autonomy.abc.framework.state.TestState;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class StateHelperRule extends TestWatcher {
    private TestState testState = TestState.get();

    public StateHelperRule(SeleniumTest<?, ?> test) {
        testState.setTest(test);
    }

    @Override
    protected void starting(Description description) {
        testState.setMethod(description);
    }

    @Override
    protected void finished(Description description) {
        testState.finish();
    }
}

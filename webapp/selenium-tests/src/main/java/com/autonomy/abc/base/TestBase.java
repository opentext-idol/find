package com.autonomy.abc.base;

import com.hp.autonomy.frontend.selenium.application.Application;
import com.hp.autonomy.frontend.selenium.application.ElementFactoryBase;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.junit.Before;
import org.junit.Rule;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assumeThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

public abstract class TestBase<A extends Application<? extends F>, F extends ElementFactoryBase> extends HybridAppTestBase<A, F> {

    @Rule
    public TestApplication annotation = new TestApplication();

    private final Type application;

    protected TestBase(TestConfig config, A appUnderTest) {
        super(config, appUnderTest);

        application = Type.valueOf(System.getProperty("application"));
    }

    @Before
    public void before(){
        if(application != null) {
            assumeThat(annotation.getApplicationValue(), anyOf(is(Type.ALL), is(application)));
        }
    }
}

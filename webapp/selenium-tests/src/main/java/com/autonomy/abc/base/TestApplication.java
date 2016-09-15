package com.autonomy.abc.base;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class TestApplication extends TestWatcher {
    private Application application;

    @Override
    protected void starting(Description description) {
        application = description.getTestClass().getAnnotation(Application.class);

        Application methodAnnotation = description.getAnnotation(Application.class);
        if(methodAnnotation != null) {
            application = methodAnnotation;
        }
    }

    public Type getApplicationValue() {
        if (application == null) {
            return Type.ALL;
        }

        return application.value();
    }
}

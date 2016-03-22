package com.hp.autonomy.frontend.selenium.base;

public interface TearDown<T extends SeleniumTest<?, ?>> {
    void tearDown(T test);
}

package com.autonomy.abc.config;

public interface TearDown<T extends SeleniumTest<?, ?>> {
    void tearDown(T test);
}

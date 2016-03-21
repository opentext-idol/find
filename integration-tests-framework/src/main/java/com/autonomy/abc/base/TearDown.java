package com.autonomy.abc.base;

import com.autonomy.abc.base.SeleniumTest;

public interface TearDown<T extends SeleniumTest<?, ?>> {
    void tearDown(T test);
}

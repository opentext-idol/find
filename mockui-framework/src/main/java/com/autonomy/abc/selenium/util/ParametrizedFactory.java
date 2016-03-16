package com.autonomy.abc.selenium.util;

public interface ParametrizedFactory<K, V> {
    V create(K context);
}

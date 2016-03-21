package com.hp.autonomy.frontend.selenium.util;

public interface ParametrizedFactory<K, V> {
    V create(K context);
}

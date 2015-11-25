package com.hp.autonomy.frontend.find.idol.aci;

/**
 * Created by milleriv on 25/11/2015.
 */
public interface AciResponseProcessorCallback<T, R> {
    R process(final T responseData);
}

package com.autonomy.abc.selenium.application;

import org.slf4j.LoggerFactory;

public abstract class FindApplication<T> implements Application<T> {
    public static FindApplication ofType(ApplicationType type) {
        if (type != ApplicationType.HOSTED) {
            LoggerFactory.getLogger(FindApplication.class).warn("Application type " + type + " is not supported on Find - may cause problems");
        }
        return new HSODFindApplication();
    }
}

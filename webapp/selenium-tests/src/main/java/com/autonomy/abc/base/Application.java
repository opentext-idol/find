package com.autonomy.abc.base;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Application {
    Type value() default Type.ALL;
}

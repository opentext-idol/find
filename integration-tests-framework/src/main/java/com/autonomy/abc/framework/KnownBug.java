package com.autonomy.abc.framework;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface KnownBug {
    String[] value();
}

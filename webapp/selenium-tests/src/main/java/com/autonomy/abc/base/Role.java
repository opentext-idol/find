package com.autonomy.abc.base;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Role {
    UserRole value() default UserRole.ALL;
}

package com.autonomy.abc.base;

import com.autonomy.abc.selenium.find.application.UserRole;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Role {
    UserRole value();
}

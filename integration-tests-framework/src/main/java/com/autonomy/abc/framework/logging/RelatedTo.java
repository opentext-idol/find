package com.autonomy.abc.framework.logging;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation for marking JIRA tickets that may be
 * relevant, but do not qualify as @KnownBug
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RelatedTo {
    String[] value();
}

package com.hp.autonomy.frontend.find.idol.authentication;

/**
 * Find roles used with Community Server
 */
public enum FindCommunityRole {
    USER("FindUser"),
    ADMIN("FindAdmin"),
    BI("FindBI");

    private final String value;

    FindCommunityRole(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static FindCommunityRole fromValue(final String value) {
        for (final FindCommunityRole role : values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }

        throw new IllegalArgumentException("Invalid role specified: " + value);
    }
}

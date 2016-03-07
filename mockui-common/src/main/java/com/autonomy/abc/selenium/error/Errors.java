package com.autonomy.abc.selenium.error;

import java.io.Serializable;

public class Errors {
    public static class Term {
        public static final String DUPLICATE_EXISTING = "duplicate of an existing keyword";
        public static final String DUPLICATED = "is duplicated";
        public static final String QUOTES = "Terms have an odd number of quotes";
        public static final String COMMAS = "Terms may not contain commas";
        // triggers are converted toLowerCase, but this may change again
        public static final String CASE = DUPLICATE_EXISTING;
        public static final String BLANK = "No terms were supplied";
        public static final String NO_QUOTES = "Terms may not contain quotation marks";
    }

    public enum Search implements Serializable {
        NO_RESULTS("No results found"),
        UNKNOWN("An unknown error occurred executing the search action"),
        GENERAL("An error occurred retrieving results"),
        OPERATORS("Invalid use of special tokens"),
        STOPWORDS("All terms were invalid, through being stopwords"),
        BACKEND("Backend request failed"),
        HOD("Haven OnDemand returned an error while executing the search action"),
        QUOTES("Unclosed phrase"),
        NO_TEXT("No valid query text supplied"),
        BLACKLIST("All query text has been blocked by white or blacklists"),
        RELATED_CONCEPTS("An error occurred fetching top results"),
        OPENING_BOOL("Opening boolean operator"),
        CLOSING_BOOL("Terminating boolean operator");

        private final String value;

        Search(String content) {
            value = content;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static class Keywords {
        public static final String CREATING = "Error occurred creating keywords";
        public static final String DUPLICATE_BLACKLIST = "already blacklisted";
    }

    public static class User {
        public static final String CREATING = "Error! New user profile creation failed.";
        public static final String BLANK_EMAIL = "Error! Email address must not be blank";
        public static final String DUPLICATE_EMAIL = "Error! A user with this email address already exists";
        public static final String DUPLICATE_USER = "Error! User exists!";
    }

    public static class Index {
        public static final String DISPLAY_NAME = "Please enter a valid name that contains only alphanumeric characters";
        public static final String MAX_CHAR_LENGTH = "The field is limited to 100 characters";
        public static final String FIELD_NAMES = "field names can contain only lowercase alphanumeric characters";
        public static final String INVALID_INDEX = "does not exist";
        public static final String INDEX_NAME = "Please enter a valid name that contains only lowercase alphanumeric characters";
    }

    public static class Find {
        public static final String GENERAL = "An error occurred";
    }
}

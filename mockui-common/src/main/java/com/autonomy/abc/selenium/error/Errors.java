package com.autonomy.abc.selenium.error;

import java.io.Serializable;

public class Errors {
    public enum Term {
        DUPLICATE_EXISTING("duplicate of an existing keyword"),
        DUPLICATED("is duplicated"),
        QUOTES("Terms have an odd number of quotes"),
        COMMAS("Terms may not contain commas"),
        // triggers are converted toLowerCase, but this may change again
        CASE("duplicate of an existing keyword"),
        BLANK("No terms were supplied"),
        NO_QUOTES("Terms may not contain quotation marks");

        private final String value;

        Term(String text) {
            value = text;
        }

        @Override
        public String toString() {
            return value;
        }
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
        CLOSING_BOOL("Terminating boolean operator"),
        INVALID("Invalid query text");

        private final String value;

        Search(String content) {
            value = content;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public enum Keywords implements Serializable {
        CREATING("Error occurred creating keywords"),
        DUPLICATE_BLACKLIST("already blacklisted");

        private final String value;

        Keywords(String text) {
            value = text;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public enum User implements Serializable {
        CREATING("Error! New user profile creation failed."),
        BLANK_EMAIL("Error! Email address must not be blank"),
        DUPLICATE_EMAIL("Error! A user with this email address already exists"),
        DUPLICATE_USER("Error! User exists!");

        private final String value;

        User(String text) {
            value = text;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public enum Index implements Serializable {
        DISPLAY_NAME("Please enter a valid name that contains only alphanumeric characters"),
        MAX_CHAR_LENGTH("The field is limited to 100 characters"),
        FIELD_NAMES("field names can contain only lowercase alphanumeric characters"),
        INVALID_INDEX("does not exist"),
        INDEX_NAME("Please enter a valid name that contains only lowercase alphanumeric characters");

        private final String value;

        Index(String text) {
            value = text;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public enum Find implements Serializable {
        GENERAL("An error occurred");

        private final String value;

        Find(String text) {
            value = text;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}

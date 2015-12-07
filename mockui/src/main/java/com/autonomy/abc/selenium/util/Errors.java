package com.autonomy.abc.selenium.util;

public class Errors {
    public static class Term {
        public static final String DUPLICATE_EXISTING = "duplicate of an existing keyword";
        public static final String DUPLICATED = "is duplicated";
        public static final String QUOTES = "Terms have an odd number of quotes";
        public static final String COMMAS = "Terms may not contain commas";
        // triggers are converted toLowerCase, but this may change again
        public static final String CASE = DUPLICATE_EXISTING;
    }

    public static class Search {
        public static final String NO_RESULTS = "No results found";
        public static final String GENERAL = "An error occurred retrieving results";
        public static final String OPERATORS = "Invalid use of special tokens";
        public static final String STOPWORDS = "All terms were invalid, through being stopwords";
        public static final String BACKEND = "Backend request failed";
        public final static String HOD = "Haven OnDemand returned an error while executing the search action";
    }

    public static class Keywords {
        public static final String CREATING = "Error occurred creating keywords";
        public static final String DUPLICATE_BLACKLIST = "already blacklisted";
    }

    public static class User {
        public static final String CREATING = "Error! New user profile creation failed.";
        public static final String BLANK_EMAIL = "Error! Email address must not be blank";
    }
}

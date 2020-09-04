package com.hp.autonomy.frontend.find.idol.pollingdata;

class RecordFormatException extends Exception {

    public RecordFormatException(
        final String fieldName, final String fieldValue, final Throwable cause
    ) {
        super("CSV record has invalid field: " + fieldName + ": " + fieldValue, cause);
    }

    public RecordFormatException(final String fieldName, final String fieldValue) {
        this(fieldName, fieldValue, null);
    }

    public RecordFormatException(final String message) {
        super(message);
    }

}

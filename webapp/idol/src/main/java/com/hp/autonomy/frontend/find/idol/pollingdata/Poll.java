package com.hp.autonomy.frontend.find.idol.pollingdata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.apache.commons.csv.CSVRecord;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class Poll {
    private static final Instant MIN_DATE = Instant.ofEpochSecond(0);
    private static final DateTimeFormatter DATA_DATE_FORMAT = DateTimeFormatter.ofPattern("M/d/uu");
    private static final ZoneId DATA_TIMEZONE = ZoneOffset.ofHours(-4);

    @JsonIgnore private final String id;
    private final List<Result> results;
    private final int sampleSize;
    private final Instant date;
    private final String sourceGrade;

    public static String csvGetField(
        final CSVRecord record, final String name, final String defaultValue
    )
        throws RecordFormatException
    {
        final String value = record.get(name);
        if (value == null || value.isEmpty()) {
            if (defaultValue == null) {
                throw new RecordFormatException(name, "<value missing>");
            } else {
                return defaultValue;
            }
        } else {
            return value;
        }
    }


    @Getter
    public static class Result {
        private final String party;
        private final double proportion;

        private static double parseProportion(final CSVRecord record) throws RecordFormatException {
            final String rawValue = csvGetField(record, "pct", null);
            final double value;
            try {
                value = Double.parseDouble(rawValue) / 100;
            } catch (final NumberFormatException e) {
                throw new RecordFormatException("pct", rawValue, e);
            }

            if (value > 1 || value < 0) {
                throw new RecordFormatException("pct", rawValue);
            }
            return value;
        }

        public Result(final CSVRecord record) throws RecordFormatException {
            party = csvGetField(record, "candidate_party", null);
            proportion = parseProportion(record);
        }

    }


    private static int parseSampleSize(final CSVRecord record) throws RecordFormatException {
        final String rawValue = csvGetField(record, "sample_size", null);
        final int value;
        try {
            value = Integer.parseInt(rawValue);
        } catch (final NumberFormatException e) {
            throw new RecordFormatException("sample_size", rawValue, e);
        }

        if (value < 0) {
            throw new RecordFormatException("sample_size", rawValue);
        }
        return value;
    }

    private static Instant parseDate(final CSVRecord record, final String fieldName)
        throws RecordFormatException {
        final String rawValue = csvGetField(record, fieldName, null);
        final Instant value;
        try {
            value = LocalDate.from(DATA_DATE_FORMAT.parse(rawValue))
                .atStartOfDay(DATA_TIMEZONE)
                .toInstant();
        } catch (final DateTimeParseException e) {
            throw new RecordFormatException(fieldName, rawValue, e);
        }

        if (value.isBefore(MIN_DATE) || value.isAfter(Instant.now())) {
            throw new RecordFormatException(fieldName, rawValue);
        }
        return value;
    }

    public Poll(final CSVRecord record) throws RecordFormatException {
        id = csvGetField(record, "poll_id", null);
        results = Collections.singletonList(new Result(record));
        sampleSize = parseSampleSize(record);
        final Instant startDate = parseDate(record, "start_date");
        final Instant endDate = parseDate(record, "end_date");
        sourceGrade = csvGetField(record, "fte_grade", "");

        if (startDate.isAfter(endDate)) {
            throw new RecordFormatException(
                "CSV record has start date " + startDate + " after end date " + endDate);
        }
        date = startDate.plus(Duration.between(startDate, endDate).dividedBy(2));
    }

    public Poll(final List<Poll> polls) {
        final Poll main = polls.get(0);
        id = main.id;
        results = polls.stream()
            .flatMap(poll -> poll.results.stream())
            .collect(Collectors.toList());
        sampleSize = main.sampleSize;
        date = main.date;
        sourceGrade = main.sourceGrade;
    }

}

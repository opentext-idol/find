package com.hp.autonomy.frontend.find.idol.pollingdata;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.types.requests.idol.actions.tags.DateRangeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.ZoneOffset.UTC;

@Component
public class PollingDataService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PollingDataService.class);
    private static final CSVFormat DATA_FORMAT = CSVFormat.DEFAULT.withFirstRecordAsHeader();

    private final Path dataFile;

    private PollingDataService(final ConfigService<IdolFindConfig> configService) {
        dataFile = Paths.get(configService.getConfig().getPollingDataFilePath());
    }

    public List<Poll> getPolls() throws IOException {
        final List<Poll> parsedPolls = new ArrayList<>();
        try (final CSVParser parser =
                 CSVParser.parse(dataFile.toFile(), StandardCharsets.UTF_8, DATA_FORMAT)
        ) {
            for (final CSVRecord record : parser) {
                try {
                    parsedPolls.add(new Poll(record));
                } catch (final RecordFormatException e) {
                    LOGGER.warn("Skipping invalid poll record: " + e.getMessage());
                }
            }
        }

        return parsedPolls.stream()
            .collect(Collectors.groupingBy(poll -> poll.getId()))
            .values()
            .stream()
            .map(Poll::new)
            .sorted(Comparator.comparing(Poll::getDate))
            .collect(Collectors.toList());
    }


    @AllArgsConstructor
    @Getter
    private static class Count {
        private final int weighting;
        private final int value;
    }


    @Getter
    private static class Bucket {
        private final Instant min;
        private final Instant max;
        private final List<Count> counts;

        public Bucket(final Instant min, final Instant max) {
            this.min = min;
            this.max = max;
            counts = new ArrayList<>();
        }

        public void addCount(final Count count) {
            counts.add(count);
        }

        public DateRangeInfo.Value getDateRangeInfoValue() {
            long totalCount = 0;
            long totalWeighting = 0;
            for (final Count count : counts) {
                totalCount += count.weighting * count.value;
                totalWeighting += count.weighting;
            }

            return new DateRangeInfo.Value(
                min.atZone(UTC), max.atZone(UTC),
                totalCount == 0 ? 0 : (int) (totalCount / totalWeighting));
        }

    }

    @Getter
    private static class Buckets {
        private Instant minValue;
        private Instant maxValue;
        private final List<Bucket> buckets;

        public Buckets(final Duration bucketSize, final Instant min, final Instant max) {
            minValue = max;
            maxValue = min;
            buckets = new ArrayList<>();

            Instant bucketMin = min;
            while (bucketMin.isBefore(max)) {
                buckets.add(new Bucket(bucketMin, bucketMin.plus(bucketSize)));
                bucketMin = bucketMin.plus(bucketSize);
            }
        }

        public void addPoll(final Poll poll, final Poll.Result result) {
            final Instant date = poll.getDate();
            for (final Bucket bucket : buckets) {
                if (!date.isBefore(bucket.getMin()) && date.isBefore(bucket.getMax())) {
                    minValue = (minValue == null || date.isBefore(minValue)) ? date : minValue;
                    maxValue = (maxValue == null || date.isAfter(maxValue)) ? date : maxValue;
                    bucket.addCount(
                        new Count(poll.getSampleSize(), (int) (result.getProportion() * 1000)));
                    break;
                }
            }
        }

    }


    // result counts are per-mille
    public List<DateRangeInfo> getBucketedPolls(
        final int bucketCount, final Instant min, final Instant max
    ) throws IOException {
        final List<Poll> polls = getPolls();
        final Duration bucketSize = Duration.between(min, max).dividedBy(bucketCount);

        final Map<String, Buckets> bucketsByParty = new HashMap<>();
        polls.stream()
            .flatMap(poll -> poll.getResults().stream())
            .map(result -> result.getParty())
            .collect(Collectors.toSet())
            .forEach(party -> {
                bucketsByParty.put(party, new Buckets(bucketSize, min, max));
            });

        for (final Poll poll : polls) {
            for (final Poll.Result result : poll.getResults()) {
                bucketsByParty.get(result.getParty()).addPoll(poll, result);
            }
        }

        final List<DateRangeInfo> results = new ArrayList<>();
        for (final String party : bucketsByParty.keySet()) {
            final Buckets buckets = bucketsByParty.get(party);
            final List<DateRangeInfo.Value> values = buckets.getBuckets().stream()
                .map(Bucket::getDateRangeInfoValue)
                .filter(value -> value.getCount() != 0)
                .collect(Collectors.toList());

            results.add(DateRangeInfo.builder()
                .id(party)
                .displayName(party)
                .count(values.stream().mapToInt(value -> value.getCount()).sum())
                .min(buckets.getMinValue().atZone(UTC))
                .max(buckets.getMaxValue().atZone(UTC))
                .bucketSize(bucketSize)
                .values(values)
                .build());
        }

        return results;
    }

}

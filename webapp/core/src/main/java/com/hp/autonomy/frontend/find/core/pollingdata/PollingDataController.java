package com.hp.autonomy.frontend.find.core.pollingdata;

import com.hp.autonomy.types.requests.idol.actions.tags.DateRangeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping(PollingDataController.BASE_PATH)
public class PollingDataController {
    public static final String BASE_PATH = "/api/public/pollingdata";
    private static final String BUCKETS_PATH = "/buckets";
    private static final String BUCKET_COUNT_PARAM = "bucketCount";
    private static final String BUCKET_MIN_PARAM = "bucketMin";
    private static final String BUCKET_MAX_PARAM = "bucketMax";
    private static final String MIN_DATE_PARAM = "minDate";
    private static final String MAX_DATE_PARAM = "maxDate";

    private final PollingDataService service;

    private PollingDataController(@Autowired final PollingDataService service) throws IOException {
        this.service = service;
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<Poll> getPollingData() throws IOException {
        return service.getPolls();
    }

    @RequestMapping(path = BUCKETS_PATH)
    public List<DateRangeInfo> getPollingDataInBuckets(
        @RequestParam(BUCKET_COUNT_PARAM) final Integer bucketCount,
        @RequestParam(BUCKET_MIN_PARAM)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final ZonedDateTime bucketMin,
        @RequestParam(BUCKET_MAX_PARAM)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final ZonedDateTime bucketMax,
        @RequestParam(value = MIN_DATE_PARAM, required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final ZonedDateTime minDate,
        @RequestParam(value = MAX_DATE_PARAM, required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final ZonedDateTime maxDate
    ) throws IOException {
        final Instant min = (
            (minDate == null || minDate.isBefore(bucketMin)) ? bucketMin : minDate
        ).toInstant();
        final Instant max = (
            (maxDate == null || maxDate.isAfter(bucketMax)) ? bucketMax : maxDate
        ).toInstant();
        return service.getBucketedPolls(bucketCount, min, max);
    }

}

package com.hp.autonomy.frontend.find.hod.search;

import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.textindex.query.search.Sort;
import com.hp.autonomy.hod.client.api.textindex.query.search.Summary;
import lombok.Data;
import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;

@Data
public class QueryParams {
    private String text;
    private int maxResults;
    private Summary summary;
    private List<ResourceIdentifier> index;
    private String fieldText;
    private Sort sort;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private DateTime minDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private DateTime maxDate;
}

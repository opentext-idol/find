package com.hp.autonomy.frontend.find.core.search;

import lombok.Data;
import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.List;

@Data
public class FindQueryParams<S extends Serializable> {
    private String text;
    private int maxResults;
    private String summary;
    private List<S> index;
    private String fieldText;
    private String sort;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private DateTime minDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private DateTime maxDate;
}

package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.types.Identifier;
import lombok.Data;
import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;

@Data
public class FindQueryParams<I extends Identifier> {
    private String text;
    private int maxResults;
    private String summary;
    private List<I> index;
    private String fieldText;
    private String sort;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private DateTime minDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private DateTime maxDate;
}

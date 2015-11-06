package com.hp.autonomy.frontend.find.web;

import com.hp.autonomy.hod.client.error.HodErrorCode;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class HodErrorResponse extends ErrorResponse {

    private final HodErrorCode hodErrorCode;

    public HodErrorResponse(final String message, final HodErrorCode hodErrorCode) {
        super(message);
        this.hodErrorCode = hodErrorCode;
    }
}

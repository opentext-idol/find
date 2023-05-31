/*
 * Copyright 2020 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.idol.testutil;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Clock whose time is only changed manually.
 */
public class TestClock extends Clock {
    private ZoneId zone;
    private Instant time;

    /**
     * Create with an initial time of 0 (epoch seconds).
     */
    public TestClock() {
        time = Instant.ofEpochSecond(0);
    }

    /**
     * @param time Initial time
     */
    public TestClock(final Instant time) {
        this.time = time;
    }

    private TestClock(final ZoneId zone, final Instant time) {
        this.zone = zone;
        this.time = time;
    }

    /**
     * @param time New time
     */
    public void setTime(final Instant time) {
        this.time = time;
    }

    /**
     * @param duration Amount of time to move into the future
     */
    public void tick(final Duration duration) {
        time = time.plus(duration);
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(final ZoneId zone) {
        return new TestClock(zone, time);
    }

    @Override
    public Instant instant() {
        return time;
    }
}

/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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

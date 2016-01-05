package com.autonomy.abc.selenium.util;

public class Waits {

    public static void loadOrFadeWait() {
        try {
            Thread.sleep(1000);
        } catch (final InterruptedException e) {/*NOOP*/}
    }

    public static void waitForGritterToClear() throws InterruptedException {
        Thread.sleep(3008);
    }
}

package com.autonomy.abc.selenium.util;

public class Wait {

    public static void loadOrFadeWait() {
        try {
            Thread.sleep(1000);
        } catch (final InterruptedException e) {/*NOOP*/}
    }

    public static void waitForGritterToClear() throws InterruptedException {
        Thread.sleep(3008);
    }
}

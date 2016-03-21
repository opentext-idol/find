package com.hp.autonomy.frontend.selenium.framework.artifacts;

import com.hp.autonomy.frontend.selenium.control.Session;
import com.hp.autonomy.frontend.selenium.control.Window;


class ArtifactSaveVisitor {
    private String currentLocation;
    private int sessionIndex;
    private int windowIndex;

    public void visit(ArtifactSaver saver) {
        sessionIndex = 0;
        for (Session session : saver.getSessions()) {
            ScreenshotSaver screenshotSaver = new ScreenshotSaver(session.getDriver());
            PageSourceSaver pageSourceSaver = new PageSourceSaver(session.getDriver());

            windowIndex = 0;
            for (Window window : session) {
                window.activate();
                updateLocation(saver);
                screenshotSaver.saveTo(pngLocation());
                pageSourceSaver.saveTo(htmlLocation());
                windowIndex++;
            }

            sessionIndex++;
        }
    }

    private void updateLocation(ArtifactSaver saver) {
        currentLocation = saver.baseLocation() + "_s" + sessionIndex + "w" + windowIndex;
    }

    private String pngLocation() {
        return currentLocation + ".png";
    }

    private String htmlLocation() {
        return currentLocation + ".html";
    }

    interface ArtifactSaver {
        String baseLocation();
        Iterable<Session> getSessions();
    }
}

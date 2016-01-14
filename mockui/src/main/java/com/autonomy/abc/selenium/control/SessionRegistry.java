package com.autonomy.abc.selenium.control;

import com.autonomy.abc.selenium.util.Factory;
import org.openqa.selenium.WebDriver;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SessionRegistry implements Iterable<Session> {
    private final Factory<? extends WebDriver> driverFactory;

    private final Set<Session> sessions;
    public SessionRegistry(Factory<? extends WebDriver> webDriverFactory) {
        driverFactory = webDriverFactory;
        sessions = new HashSet<>();
    }

    public Session startSession(String url) {
        Session session = startSession();
        session.openWindow(url);
        return session;
    }

    public Session startSession() {
        Session session = new Session(driverFactory.create());
        sessions.add(session);
        return session;
    }

    /** Call this to terminate the Session safely.
     * This Session should not be used again afterwards.
     */
    public void endSession(Session session) {
        sessions.remove(session);
        session.end();
    }

    @Override
    public Iterator<Session> iterator() {
        return sessions.iterator();
    }
}

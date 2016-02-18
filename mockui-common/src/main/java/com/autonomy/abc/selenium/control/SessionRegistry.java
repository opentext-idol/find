package com.autonomy.abc.selenium.control;

import com.autonomy.abc.selenium.util.Factory;
import com.autonomy.abc.selenium.util.ParametrizedFactory;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SessionRegistry implements Iterable<Session> {
    private final Factory<? extends WebDriver> driverFactory;
    private ParametrizedFactory<Session, Window> windowFactory;
    private final List<Session> sessions;

    public SessionRegistry(Factory<? extends WebDriver> webDriverFactory, ParametrizedFactory<Session, Window> newWindowFactory) {
        driverFactory = webDriverFactory;
        sessions = new ArrayList<>();
        windowFactory = newWindowFactory;
    }

    public Session startSession(String url) {
        Session session = startSession();
        session.setUrl(session.getActiveWindow(), url);
        return session;
    }

    public Session startSession() {
        Session session = new Session(driverFactory.create(), windowFactory);
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

    public void clear() {
        for (Session session : this) {
            endSession(session);
        }
        assert sessions.isEmpty();
    }

    @Override
    public Iterator<Session> iterator() {
        return new ArrayList<>(sessions).iterator();
    }
}

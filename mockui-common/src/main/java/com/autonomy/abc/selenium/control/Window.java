package com.autonomy.abc.selenium.control;

public class Window {
    private final Session parent;
    private final String handle;

    Window(Session session, String driverWindowHandle) {
        if (driverWindowHandle == null) {
            throw new NullPointerException();
        }
        parent = session;
        handle = driverWindowHandle;
    }

    public String getUrl() {
        return parent.getUrl(this);
    }

    public void goTo(String url) {
        parent.setUrl(this, url);
    }

    public void close() {
        parent.closeWindow(this);
    }

    public Session getSession() {
        return parent;
    }

    public void activate() {
        parent.switchWindow(this);
    }

    String getHandle() {
        return handle;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Window && handle.equals(((Window) obj).getHandle());
    }

    public void refresh(){
        this.activate();
        parent.getDriver().navigate().refresh();
    }
}

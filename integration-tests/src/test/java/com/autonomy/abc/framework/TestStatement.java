package com.autonomy.abc.framework;

import org.hamcrest.Description;
import org.junit.rules.TestName;

public class TestStatement extends TestName {
    private boolean pass;
    private String name;
    private Description description;
    private String id;

    public TestStatement(String name, Description description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setId(String methodName, int number) {
        id = methodName + "#" + number;
    }

    public String getId() {
        return id;
    }

    public boolean passed() {
        return pass;
    }

    public boolean failed() {
        return !pass;
    }

    public void setState(boolean pass) {
        this.pass = pass;
    }

    public String toString() {
        StringBuilder msg = new StringBuilder();
        msg.append(passed() ? "ok " : "not ok ");
        msg.append(id).append(": ").append(name);
        if (failed()) {
            msg.append("\n").append(description).append("\n");
        }
        return msg.toString();
    }
}

package com.autonomy.abc.framework.state;

import org.hamcrest.Description;
import org.junit.rules.TestName;

public class TestStatement extends TestName {
    private boolean pass;
    private String name;
    private Description description;
    private String id;
    private int number;
    private String methodName;

    TestStatement(String name, Description description) {
        this.name = name;
        this.description = description;
    }

    void setId(String methodName, int number) {
        id = methodName + "#" + number;
        this.number = number;
        this.methodName = methodName;
    }

    void setState(boolean pass) {
        this.pass = pass;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    public boolean passed() {
        return pass;
    }

    public boolean failed() {
        return !pass;
    }

    @Override
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

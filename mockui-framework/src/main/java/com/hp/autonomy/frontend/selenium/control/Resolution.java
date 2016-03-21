package com.hp.autonomy.frontend.selenium.control;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;

public class Resolution {
    public final static Resolution MAXIMIZED = new Resolution() {
        @Override
        public void applyTo(WebDriver.Window window) {
            window.maximize();
        }
    };

    private Dimension dimension;

    private Resolution() {}

    public Resolution(int width, int height) {
        this.dimension = new Dimension(width, height);
    }

    public void applyTo(WebDriver.Window window) {
        window.setSize(dimension);
    }
}

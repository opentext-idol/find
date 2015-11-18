package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.indexes.Index;

public class SecureWebConnector extends WebConnector {
    private final Credentials credentials;

    public SecureWebConnector(String url, String name, Credentials credentials) {
        super(url, name);
        this.credentials = credentials;
    }

    public SecureWebConnector(String url, String name, Index index, Credentials credentials) {
        super(url, name, index);
        this.credentials = credentials;
    }

    public Credentials getCredentials() {
        return credentials;
    }
}

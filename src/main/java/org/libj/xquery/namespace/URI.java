package org.libj.xquery.namespace;

public class URI implements Symbol {
    private String uri;

    public URI(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }
}

package org.libj.xquery.compiler;

public class ClassInfo {
    private byte[] bytes;
    private String className;

    public ClassInfo(String className, byte[] bytes) {
        this.className = className;
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getClassName() {
        return className;
    }
}

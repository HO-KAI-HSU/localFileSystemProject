package com.example.localfilesystemproject.storage.enumObj;

public enum OrderByEnum {
    lastModified("lastModified"),
    size("size"),
    fileName("fileName");

    private String value;

    private OrderByEnum(String value) { this.value = value; }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

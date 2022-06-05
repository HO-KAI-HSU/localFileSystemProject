package com.example.localfilesystemproject.storage.enumObj;


import java.util.stream.Stream;

public enum OrderByDirectionEnum {
    Descending("Descending"),
    Ascending("Ascending");

    private String value;

    private OrderByDirectionEnum(String value) { this.value = value; }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

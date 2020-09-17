package com.example.demo;

import java.util.ArrayList;
import java.util.List;

public class CreationList {
    private List<String> values;

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public CreationList() {
        this.values = new ArrayList<>();
    }
}
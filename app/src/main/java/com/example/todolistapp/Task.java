package com.example.todolistapp;

public class Task {
    private String name;
    private String dateTime;

    public Task(String name, String dateTime) {
        this.name = name;
        this.dateTime = dateTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return name + " (" + dateTime + ")";
    }
}


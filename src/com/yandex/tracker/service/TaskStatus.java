package com.yandex.tracker.service;

public enum TaskStatus {
    NEW("Задача создана."),
    IN_PROGRESS("Над задачей ведётся работа."),
    DONE("Задача выполнена.");

    private final String description;

    TaskStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
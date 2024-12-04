package com.yandex.tracker.service;

import java.io.File;

public class Managers {

    private Managers() {
    }

    public static TaskManager getDefault() {
        return new FileBackedTaskManager(new File("resources/task.csv"));
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}

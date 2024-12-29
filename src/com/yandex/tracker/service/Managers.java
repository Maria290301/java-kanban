package com.yandex.tracker.service;

import com.yandex.tracker.exception.ManagerSaveException;

import java.io.File;

public class Managers {

    private Managers() {
    }

    public static TaskManager getDefault() throws ManagerSaveException {
        return new FileBackedTaskManager(new File("resources/task.csv"));
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}

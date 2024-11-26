package com.yandex.tracker.service;

import com.yandex.tracker.model.Task;

import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private static final int MAX_HISTORY_SIZE = 10;
    private final LinkedList<Task> history = new LinkedList<>();

    @Override
    public void add(Task task) {
        if (task != null) {
            if (history.size() >= MAX_HISTORY_SIZE) {
                history.removeFirst();
                history.add(task);
            } else {
                history.add(task);
            }
        }
    }

    @Override
    public List<Task> getHistory() {
        return history;
    }
}

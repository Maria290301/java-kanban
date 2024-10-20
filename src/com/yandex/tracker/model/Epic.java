package com.yandex.tracker.model;

import com.yandex.tracker.service.TaskStatus;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    List<Subtask> subtasks = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
    }

    public TaskStatus getStatus() {
        return super.getStatus();
    }

    public void setStatus(TaskStatus status) {
        super.setStatus(status);
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(List<Subtask> subtasks) {
        this.subtasks = subtasks;
    }

    @Override
    public String toString() {
        return nameTask + ": " +
                subtasks;
    }
}
package com.yandex.tracker.model;

import com.yandex.tracker.service.TaskStatus;
import com.yandex.tracker.service.TaskType;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Subtask> subtasks = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
        this.taskType = TaskType.EPIC;
    }

    public Epic(int id, String name, String description) {
        super(id, name, description, TaskStatus.NEW);
        this.taskType = TaskType.EPIC;
    }

    public void addSubtask(Subtask subtask) {
        subtasks.add(subtask);
    }

    public void removeSubtask(int subtaskId) {
        subtasks.removeIf(subtask -> subtask.getId() == subtaskId);
    }

    public void cleanSubtasks() {
        subtasks.clear();
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
        for (Subtask subtask : subtasks) {
            if (subtask.getEpicId() == this.getId()) {
                throw new IllegalArgumentException("Эпик не может добавлять себя в качестве подзадачи.");
            }
        }
        this.subtasks = subtasks;
    }

    @Override
    public String toString() {
        return nameTask + ": " +
                subtasks;
    }
}
package com.yandex.tracker.model;

import com.yandex.tracker.service.TaskStatus;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String nameTask, String descriptionTask, TaskStatus status, int epicId) {
        this.nameTask = nameTask;
        this.descriptionTask = descriptionTask;
        this.status = status;
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
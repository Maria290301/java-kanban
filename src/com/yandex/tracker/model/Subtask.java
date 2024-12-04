package com.yandex.tracker.model;

import com.yandex.tracker.service.TaskStatus;

public class Subtask extends Task {
    private static int idCounter = 0;
    private int epicId;
    private int id;

    public Subtask(String nameTask, String descriptionTask, TaskStatus status, int epicId) {
        this.nameTask = nameTask;
        this.descriptionTask = descriptionTask;
        this.status = status;
        this.epicId = epicId;
        this.id = ++idCounter;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
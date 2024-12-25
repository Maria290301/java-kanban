package com.yandex.tracker.model;

import com.yandex.tracker.service.TaskStatus;
import com.yandex.tracker.service.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private int epicId;

    public Subtask(int id, String nameTask, String descriptionTask, TaskStatus status, Duration duration,
                   LocalDateTime startTime, int epicId) {
        super(id, nameTask, descriptionTask, status, TaskType.SUBTASK, duration, startTime);
        this.epicId = epicId; // Инициализация epicId
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
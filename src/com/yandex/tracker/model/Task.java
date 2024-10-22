package com.yandex.tracker.model;

import com.yandex.tracker.service.TaskStatus;

import java.util.Objects;

public class Task {
    protected String nameTask;
    protected String descriptionTask;
    protected int id;
    protected TaskStatus status = TaskStatus.NEW;

    public Task() {
    }

    public Task(String nameTask, String descriptionTask, TaskStatus status) {
        this.nameTask = nameTask;
        this.descriptionTask = descriptionTask;
        this.status = status;
    }

    public String getNameTask() {
        return nameTask;
    }

    public void setNameTask(String nameTask) {
        this.nameTask = nameTask;
    }

    public String getDescriptionTask() {
        return descriptionTask;
    }

    public void setDescriptionTask(String descriptionTask) {
        this.descriptionTask = descriptionTask;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Task task = (Task) obj;
        return id == task.id && Objects.equals(nameTask, task.nameTask) &&
                Objects.equals(descriptionTask, task.descriptionTask) &&
                Objects.equals(status, task.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nameTask, descriptionTask, id, status);
    }

    @Override
    public String toString() {
        return nameTask;
    }
}
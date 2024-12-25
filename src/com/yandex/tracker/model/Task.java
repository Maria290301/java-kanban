package com.yandex.tracker.model;

import com.yandex.tracker.service.TaskStatus;
import com.yandex.tracker.service.TaskType;

import java.util.Objects;
import java.time.Duration;
import java.time.LocalDateTime;

public class Task {
    protected String nameTask;
    protected String descriptionTask;
    protected int id;
    protected TaskStatus status = TaskStatus.NEW;
    protected TaskType taskType;
    private Duration duration;
    private LocalDateTime startTime;

    public Task(int id, String nameTask, String descriptionTask, TaskStatus status, TaskType taskType,
                Duration duration, LocalDateTime startTime) {
        this.id = id;
        this.nameTask = nameTask;
        this.descriptionTask = descriptionTask;
        this.status = status;
        this.taskType = taskType;
        this.duration = duration;
        this.startTime = startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }


    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }


    public LocalDateTime getEndTime() {
        return startTime != null && duration != null ? startTime.plus(duration) : null;
    }

    public boolean overlapsWith(Task other) {
        if (other == null || this.startTime == null || other.startTime == null) {
            return false;
        }
        return this.startTime.isBefore(other.getEndTime()) && other.getStartTime().isBefore(this.getEndTime());
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

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Task task = (Task) obj;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nameTask;
    }
}
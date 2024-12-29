package com.yandex.tracker.model;

import com.yandex.tracker.service.TaskStatus;
import com.yandex.tracker.service.TaskType;

import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class Epic extends Task {
    private List<Subtask> subtasks;

    public Epic(int id, String nameTask, String descriptionTask, TaskStatus status, Duration duration,
                LocalDateTime startTime) {
        super(id, nameTask, descriptionTask, status, TaskType.EPIC, duration, startTime);
        this.subtasks = new ArrayList<>();
    }

    @Override
    public Duration getDuration() {
        return subtasks.stream()
                .map(Subtask::getDuration)
                .reduce(Duration.ZERO, Duration::plus);
    }

    @Override
    public LocalDateTime getStartTime() {
        return subtasks.stream()
                .map(Subtask::getStartTime)
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }

    @Override
    public LocalDateTime getEndTime() {
        return subtasks.stream()
                .map(subtask -> subtask.getStartTime().plus(subtask.getDuration()))
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    public void addSubtask(Subtask subtask) {
        for (Subtask existingSubtask : subtasks) {
            if (subtask.overlapsWith(existingSubtask)) {
                throw new IllegalArgumentException("Подзадача пересекается с существующей подзадачей.");
            }
        }
        subtasks.add(subtask);
        updateStatus();
    }

    public void removeSubtask(int subtaskId) {
        subtasks.removeIf(subtask -> subtask.getId() == subtaskId);
        updateStatus();
    }

    public void cleanSubtasks() {
        if (subtasks != null) {
            subtasks.clear();
        }
    }

    private void updateStatus() {
        if (subtasks.isEmpty()) {
            setStatus(TaskStatus.NEW);
            return;
        }
        boolean hasNew = subtasks.stream().anyMatch(subtask -> subtask.getStatus() == TaskStatus.NEW);
        boolean hasInProgress = subtasks.stream().anyMatch(subtask -> subtask.getStatus() == TaskStatus.IN_PROGRESS);
        boolean hasDone = subtasks.stream().allMatch(subtask -> subtask.getStatus() == TaskStatus.DONE);

        if (hasNew) {
            setStatus(TaskStatus.IN_PROGRESS);
        } else if (hasDone) {
            setStatus(TaskStatus.DONE);
        } else {
            setStatus(TaskStatus.NEW);
        }
    }

    public TaskStatus getStatus() {
        return super.getStatus();
    }

    public void setStatus(TaskStatus status) {
        super.setStatus(status);
    }

    public List<Subtask> getSubtasks() {
        return new ArrayList<>(this.subtasks);
    }

    public void setSubtasks(List<Subtask> subtasks) {
        for (Subtask subtask : subtasks) {
            if (subtask.getEpicId() == this.getId()) {
                throw new IllegalArgumentException("Эпик не может добавлять себя в качестве подзадачи.");
            }
        }
        this.subtasks = subtasks;
    }

    public List<Subtask> getEpicSubtasks(int epicId) {
        return subtasks.stream()
                .filter(subtask -> subtask.getEpicId() == epicId)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return nameTask + ": " +
                subtasks;
    }
}

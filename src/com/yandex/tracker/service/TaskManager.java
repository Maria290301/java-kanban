package com.yandex.tracker.service;

import com.yandex.tracker.model.Epic;
import com.yandex.tracker.model.Subtask;
import com.yandex.tracker.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskManager {
    private static int countID = 0;
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();

    public int createTask(Task task) {
        countID++;
        task.setId(countID);
        tasks.put(task.getId(), task);
        return task.getId();
    }

    public Integer createSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            return null;
        }
        subtask.setId(++countID);
        subtasks.put(subtask.getId(), subtask);
        epic.getSubtasks().add(subtask);
        updateEpicStatus(epic.getId());
        return subtask.getId();
    }

    public int createEpic(Epic epic) {
        countID++;
        epic.setId(countID);
        epics.put(epic.getId(), epic);
        return epic.getId();
    }

    public void removeTasks() {
        tasks.clear();
    }

    public void removeEpics() {
        subtasks.clear();
        epics.clear();
    }

    public void removeSubtasks() {
        for (Epic epic : epics.values()) {
            epic.getSubtasks().clear();
            updateEpicStatus(epic.getId());
        }
        subtasks.clear();
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic.getId());
        }
    }

    public void updateEpic(Epic epic) {
        Epic existingEpic = epics.get(epic.getId());
        existingEpic.setDescriptionTask(epic.getDescriptionTask());
        existingEpic.setSubtasks(epic.getSubtasks());
        updateEpicStatus(existingEpic.getId());
        epics.put(epic.getId(), existingEpic);
    }

    public void removeTaskById(int id) {
        Task task = getTaskById(id);
        if (task != null) {
            tasks.remove(task.getId());
        }
    }

    public void removeSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            int epicId = subtask.getEpicId();
            Epic epic = epics.get(epicId);
            if (epic != null) {
                epic.getSubtasks().remove(subtask);
                updateEpicStatus(epic.getId());
            }
            subtasks.remove(subtask.getId());
        }
    }

    public void removeEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            for (Subtask subtask : epic.getSubtasks()) {
                subtasks.remove(subtask.getId());
            }
            epics.remove(id);
        }
    }

    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public List<Subtask> getEpicSubtasks(int epicId) {

        List<Subtask> epicSubtasks = new ArrayList<>();
        for (Subtask subtask : subtasks.values()) {
            if (subtask.getEpicId() == epicId) {
                epicSubtasks.add(subtask);
            }
        }
        return epicSubtasks;
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = getEpicById(epicId);
        if (epic != null) {
            List<Subtask> subtasks = getEpicSubtasks(epicId);
            boolean allDone = true;
            boolean anyInProgress = false;
            for (Subtask subtask : subtasks) {
                if (subtask.getStatus() == TaskStatus.NEW) {
                    allDone = false;
                    break;
                }
                if (subtask.getStatus() == TaskStatus.IN_PROGRESS) {
                    anyInProgress = true;
                }
            }
            if (allDone) {
                epic.setStatus(TaskStatus.DONE);
            } else if (anyInProgress) {
                epic.setStatus(TaskStatus.IN_PROGRESS);
            } else {
                epic.setStatus(TaskStatus.NEW);
            }
        }
    }
}
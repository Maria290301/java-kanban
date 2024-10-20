package com.yandex.tracker.service;

import com.yandex.tracker.model.Epic;
import com.yandex.tracker.model.Subtask;
import com.yandex.tracker.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskManager {
    private static int countID = 0;
    HashMap<Integer, Task> taskList = new HashMap<>();
    HashMap<Integer, Subtask> subtaskList = new HashMap<>();
    HashMap<Integer, Epic> epicLists = new HashMap<>();

    public void createTask(Task task) {
        countID++;
        task.setId(countID);
        taskList.put(task.getId(), task);
    }

    public void createSubtask(Subtask subtask) {
        subtask.setId(++countID);
        subtaskList.put(subtask.getId(), subtask);
        Epic epic = epicLists.get(subtask.getEpicId());
        if (epic != null) {
            epic.getSubtasks().add(subtask);
            updateEpicStatus(epic);
        }
    }

    public void createEpic(Epic epic) {
        countID++;
        epic.setId(countID);
        epicLists.put(epic.getId(), epic);
    }

    public void removeAllTasks() {
        taskList.clear();
    }

    public void removeAllEpics() {
        subtaskList.clear();
        epicLists.clear();
    }

    public Task getTaskById(int id) {
        return taskList.get(id);
    }

    public Subtask getSubtaskId(int id) {
        return subtaskList.get(id);
    }

    public Epic getEpicById(int id) {
        return epicLists.get(id);
    }

    public void updateTask(Task task) {
        int id = 0;
        taskList.put(id, task);
    }

    public void updateSubtask(int id, Subtask subtask) {
        Subtask existingSubtask = subtaskList.get(id);
        int oldEpicId = existingSubtask.getEpicId();
        int newEpicId = subtask.getEpicId();
        existingSubtask.setDescriptionTask(subtask.getDescriptionTask());
        existingSubtask.setStatus(subtask.getStatus());
        if (oldEpicId != newEpicId) {
            Epic oldEpic = epicLists.get(oldEpicId);
            if (oldEpic != null) {
                oldEpic.getSubtasks().remove(existingSubtask);
            }
            existingSubtask.setEpicId(newEpicId);
            Epic newEpic = epicLists.get(newEpicId);
            if (newEpic != null) {
                newEpic.getSubtasks().add(existingSubtask);
            }
        }
        subtaskList.put(id, existingSubtask);
    }

    public void updateEpic(int id, Epic epic) {
        Epic existingEpic = epicLists.get(id);
        existingEpic.setSubtasks(epic.getSubtasks());
        existingEpic.setStatus(epic.getStatus());
        existingEpic.setDescriptionTask(epic.getDescriptionTask());
        epicLists.put(id, existingEpic);
    }

    public void removeTaskById(int id) {
        Task task = getTaskById(id); // Предполагается, что у вас есть метод для получения задачи по ID
        if (task != null) {
            taskList.remove(task);
        }
    }

    public void removeSubtaskById(int id) {
        Subtask subtask = subtaskList.get(id);
        if (subtask != null) {
            int epicId = subtask.getEpicId();
            Epic epic = epicLists.get(epicId);
            if (epic != null) {
                epic.getSubtasks().remove(subtask);
                updateEpicStatus(epic);
            }
            Subtask remove = subtaskList.remove(subtask);
        }
    }

    public void removeEpicById(int id) {
        Epic epic = epicLists.get(id);
        if (epic != null) {
            for (Subtask subtask : epic.getSubtasks()) {
                subtaskList.remove(subtask.getId());
                epicLists.remove(id);
            }
        }
    }

    private void updateEpicStatus(Epic epicId) {
        Epic epic = getEpicById(epicId.getId());
        if (epic != null) {
            List<Subtask> subtasks = getEpicSubtasks(epicId.getId());
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

    public List<Task> getTasks() {
        return new ArrayList<>(taskList.values());
    }

    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtaskList.values());
    }

    public List<Epic> getEpics() {
        return new ArrayList<>(epicLists.values());
    }

    public List<Subtask> getEpicSubtasks(int epicId) {

        List<Subtask> epicSubtasks = new ArrayList<>();
        for (Subtask subtask : subtaskList.values()) {
            if (subtask.getEpicId() == epicId) {
                epicSubtasks.add(subtask);
            }
        }
        return epicSubtasks;
    }
}
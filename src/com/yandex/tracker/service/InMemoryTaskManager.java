package com.yandex.tracker.service;

import com.yandex.tracker.model.Epic;
import com.yandex.tracker.model.Subtask;
import com.yandex.tracker.model.Task;

import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    private static int countID = 0;
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final HistoryManager historyManager;
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime,
            Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(Task::getId));


    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

    @Override
    public int createTask(Task task) {
        if (task.getId() != 0 && tasks.containsKey(task.getId())) {
            throw new IllegalArgumentException("Задачи с одинаковым id не должны добавляться.");
        }

        task.setId(++countID);
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            addToPrioritizedTasks(task);
        }
        return task.getId();
    }

    @Override
    public Integer createSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            throw new IllegalArgumentException("Эпик с ID " + subtask.getEpicId() + "  не существует.");
        }
        if (subtask.getId() != 0 && subtasks.containsKey(subtask.getId())) {
            throw new IllegalArgumentException("Подзадачи с одинаковым id не должны добавляться.");
        }

        subtask.setId(++countID);
        subtasks.put(subtask.getId(), subtask);
        if (subtask.getStartTime() != null) {
            addToPrioritizedTasks(subtask);
        }
        epic.getSubtasks().add(subtask);
        updateEpicStatus(epic.getId());
        return subtask.getId();
    }

    @Override
    public int createEpic(Epic epic) {
        epic.setId(++countID);
        epics.put(epic.getId(), epic);
        return epic.getId();
    }

    private void addToPrioritizedTasks(Task newTask) {
        prioritizedTasks.stream()
                .filter(existingTask -> isOverlapping(newTask, existingTask))
                .findFirst()
                .ifPresentOrElse(
                        overlappedTask -> {
                            if (newTask instanceof Subtask) {
                                throw new IllegalArgumentException("Подзадача пересекается с существующей подзадачей.");
                            } else {
                                throw new IllegalArgumentException("Задача пересекается с существующей задачей.");
                            }
                        },
                        () -> prioritizedTasks.add(newTask)
                );
    }

    private boolean isOverlapping(Task task1, Task task2) {
        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = task1.getEndTime();

        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = task2.getEndTime();

        return (start1.isEqual(start2) || start1.isBefore(start2) && end1.isAfter(start2))
                || (start2.isEqual(start1) || start2.isBefore(start1) && end2.isAfter(start1));
    }

    @Override
    public void removeTasks() {
        for (Task task : tasks.values()) {
            historyManager.remove(task.getId());
            prioritizedTasks.remove(task);
        }
        tasks.clear();
    }

    @Override
    public void removeEpics() {
        for (Epic epic : epics.values()) {
            historyManager.remove(epic.getId());
        }
        epics.clear();
        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
            prioritizedTasks.remove(subtask);
        }
        subtasks.clear();
    }

    @Override
    public void removeSubtasks() {
        for (Epic epic : epics.values()) {
            epic.getSubtasks().clear();
            updateEpicStatus(epic.getId());
        }
        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
            prioritizedTasks.remove(subtask);
        }
        subtasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtasks.get(id);
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epics.get(id);
    }

    @Override
    public void updateTask(Task task) {
        prioritizedTasks.remove(tasks.get(task.getId()));
        tasks.put(task.getId(), task);
        prioritizedTasks.add(task);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        prioritizedTasks.remove(subtasks.get(subtask.getId()));
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic.getId());
        }
        prioritizedTasks.add(subtask);
    }

    @Override
    public void updateEpic(Epic epic) {
        Epic existingEpic = epics.get(epic.getId());
        existingEpic.setDescriptionTask(epic.getDescriptionTask());
        updateEpicStatus(existingEpic.getId());
        epics.put(epic.getId(), existingEpic);
    }

    @Override
    public void removeTaskById(int id) {
        Task task = getTaskById(id);
        if (task != null) {
            prioritizedTasks.remove(task);
            historyManager.remove(id);
            tasks.remove(task.getId());
        }
    }

    @Override
    public void removeSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            prioritizedTasks.remove(subtask);
            Epic epic = getEpicById(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtask(subtask.getId());
                updateEpicStatus(epic.getId());
            }
            subtasks.remove(id);
        }
    }

    @Override
    public void removeEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            for (Subtask subtask : epic.getSubtasks()) {
                historyManager.remove(subtask.getId());
                prioritizedTasks.remove(subtask);
                subtasks.remove(subtask.getId());
            }
            historyManager.remove(id);
            epics.remove(id);
        }
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {

        List<Subtask> epicSubtasks = new ArrayList<>();
        for (Subtask subtask : subtasks.values()) {
            if (subtask.getEpicId() == epicId) {
                epicSubtasks.add(subtask);
            }
        }
        return epicSubtasks;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = getEpicById(epicId);
        if (epic == null) {
            throw new IllegalArgumentException("Эпик с ID " + epicId + " не найден.");
        }

        List<Subtask> subtasks = getEpicSubtasks(epicId);
        boolean hasInProgress = false;
        boolean hasDone = false;

        for (Subtask subtask : subtasks) {
            if (subtask.getStatus() == TaskStatus.IN_PROGRESS) {
                hasInProgress = true;
            } else if (subtask.getStatus() == TaskStatus.DONE) {
                hasDone = true;
            }
        }

        if (hasInProgress) {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        } else if (hasDone) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.NEW);
        }
    }
}


package com.yandex.tracker.service;

import com.yandex.tracker.model.Epic;
import com.yandex.tracker.model.Subtask;
import com.yandex.tracker.model.Task;

import java.util.List;

public interface TaskManager {

    int createTask(Task task);

    Integer createSubtask(Subtask subtask);

    int createEpic(Epic epic);

    void removeTasks();

    void removeEpics();

    void removeSubtasks();

    Task getTaskById(int id);

    Subtask getSubtaskById(int id);

    Epic getEpicById(int id);

    void updateTask(Task task);

    void updateSubtask(Subtask subtask);

    void updateEpic(Epic epic);

    void removeTaskById(int id);

    void removeSubtaskById(int id);

    void removeEpicById(int id);

    List<Task> getTasks();

    List<Subtask> getSubtasks();

    List<Epic> getEpics();

    List<Subtask> getEpicSubtasks(int epicId);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();
}



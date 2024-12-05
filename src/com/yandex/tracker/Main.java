package com.yandex.tracker;

import com.yandex.tracker.model.Epic;
import com.yandex.tracker.model.Subtask;
import com.yandex.tracker.model.Task;
import com.yandex.tracker.service.Managers;
import com.yandex.tracker.service.TaskManager;
import com.yandex.tracker.service.TaskStatus;

public class Main {
    public static void main(String[] args) {

        TaskManager manager = Managers.getDefault();

        Task task1 = new Task("Task #1", "Task1 description", TaskStatus.NEW);
        Task task2 = new Task("Task #2", "Task2 description", TaskStatus.IN_PROGRESS);
        final int taskId1 = manager.createTask(task1);
        final int taskId2 = manager.createTask(task2);

        Epic epic1 = new Epic("Epic #1", "Epic1 description");
        Epic epic2 = new Epic("Epic #2", "Epic2 description");
        final int epicId1 = manager.createEpic(epic1);
        final int epicId2 = manager.createEpic(epic2);

        Subtask subtask1 = new Subtask("Subtask #1-1", "Subtask1 description", TaskStatus.NEW, epicId1);
        Subtask subtask2 = new Subtask("Subtask #2-1", "Subtask1 description", TaskStatus.NEW, epicId1);
        Subtask subtask3 = new Subtask("Subtask #3-2", "Subtask1 description", TaskStatus.DONE, epicId2);
        final Integer subtaskId1 = manager.createSubtask(subtask1);
        final Integer subtaskId2 = manager.createSubtask(subtask2);
        final Integer subtaskId3 = manager.createSubtask(subtask3);

        printAllTasks(manager);

        final Task task = manager.getTaskById(taskId2);
        task.setStatus(TaskStatus.DONE);
        manager.updateTask(task);
        System.out.println("CHANGE STATUS: Task2 IN_PROGRESS->DONE");
        System.out.println("Задачи:");
        for (Task t : manager.getTasks()) {
            System.out.println(t);
        }

        Subtask subtask = manager.getSubtaskById(subtaskId2);
        subtask.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask);
        System.out.println("CHANGE STATUS: Subtask2 NEW->DONE");
        subtask = manager.getSubtaskById(subtaskId3);
        subtask.setStatus(TaskStatus.NEW);
        manager.updateSubtask(subtask);
        System.out.println("CHANGE STATUS: Subtask3 DONE->NEW");
        System.out.println("Подзадачи:");
        for (Task t : manager.getSubtasks()) {
            System.out.println(t);
        }

        System.out.println("Эпики:");
        for (Task e : manager.getEpics()) {
            System.out.println(e);
            for (Task t : manager.getEpicSubtasks(e.getId())) {
                System.out.println("--> " + t);
            }
        }
        final Epic epic = manager.getEpicById(epicId1);
        epic.setStatus(TaskStatus.NEW);

        Subtask newSubtask = new Subtask("Новая подзадача для Эпика #1",
                "Описание новой подзадачи", TaskStatus.NEW, epicId1);
        manager.createSubtask(newSubtask);

        manager.updateEpic(epic);
        System.out.println("CHANGE STATUS: Epic1 IN_PROGRESS->NEW");
        printAllTasks(manager);

        System.out.println("Эпики:");
        for (Task e : manager.getEpics()) {
            System.out.println(e);
            for (Task t : manager.getEpicSubtasks(e.getId())) {
                System.out.println("--> " + t);
            }
        }

        System.out.println("DELETE: Task1");
        manager.removeTaskById(taskId1);
        System.out.println("DELETE: Epic1");
        manager.removeEpicById(epicId1);
        printAllTasks(manager);

        System.out.println("Список задач после удаления:");
        manager.removeTasks();
        manager.removeEpics();
        manager.removeSubtasks();
        printAllTasks(manager);
    }

    public static void printAllTasks(TaskManager manager) {
        if (manager.getTasks().isEmpty()) {
            System.out.println("Список задач пуст");
        } else {
            System.out.println("Задачи:");
            for (Task task : manager.getTasks()) {
                System.out.println(task);
            }

            System.out.println("Эпики:");
            for (Epic epic : manager.getEpics()) {
                System.out.println(epic);
                for (Task task : manager.getEpicSubtasks(epic.getId())) {
                    System.out.println("--> " + task);
                }
            }

            System.out.println("Подзадачи:");
            for (Task subtask : manager.getSubtasks()) {
                System.out.println(subtask);
            }

            System.out.println("История:");
            for (Task task : manager.getHistory()) {
                System.out.println(task);
            }
        }
    }
}
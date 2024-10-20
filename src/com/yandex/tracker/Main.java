package com.yandex.tracker;

import com.yandex.tracker.model.Epic;
import com.yandex.tracker.model.Subtask;
import com.yandex.tracker.model.Task;
import com.yandex.tracker.service.TaskManager;
import com.yandex.tracker.service.TaskStatus;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Запускаем тестирование:");
        TaskManager manager = new TaskManager();

        Task task1 = new Task("Задача1", "Описание задачи1", TaskStatus.NEW);
        Task task2 = new Task("Задача2", "Описание задачи2", TaskStatus.NEW);
        Task task3 = new Task("Задача3", "Описание задачи3", TaskStatus.NEW);
        manager.createTask(task1);
        manager.createTask(task2);
        manager.createTask(task3);

        Epic epic1 = new Epic("Эпик1", "Описание эпика1");
        Epic epic2 = new Epic("Эпик2", "Описание эпика2");
        manager.createEpic(epic1);
        manager.createEpic(epic2);

        Subtask subtask1 = new Subtask("Подзадача1");
        Subtask subtask2 = new Subtask("Подзадача2");
        Subtask subtask3 = new Subtask("Подзадача3");
        Subtask subtask4 = new Subtask("Подзадача4");

        subtask1.setEpicId(epic1.getId());
        subtask2.setEpicId(epic1.getId());
        subtask3.setEpicId(epic2.getId());
        subtask4.setEpicId(epic2.getId());

        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        manager.createSubtask(subtask3);
        manager.createSubtask(subtask4);

        Task task4 = new Task("Новая задача1", "Описание новой задачи1", TaskStatus.NEW);
        manager.createTask(task4);

        System.out.println("Получить список всех задач.");
        printTasks(manager);
        printEpic(manager);
        printSubtasks(manager.getSubtasks());
        System.out.println();

        System.out.println("Получить по идентификатору.");
        System.out.println(manager.getTaskById(task2.getId()));
        System.out.println(manager.getEpicById(epic1.getId()));
        System.out.println(manager.getSubtaskId(subtask2.getId()));
        System.out.println();


        if (task1.getId() != 0) {
            task1.setNameTask("Обновленная Задача1");
            task1.setDescriptionTask("Обновленное описание задачи1");
            task1.setStatus(TaskStatus.IN_PROGRESS);
            manager.updateTask(task1); // Обновляем существующую задачу
        }

        if (task2.getId() != 0) {
            task2.setNameTask("Обновленная Задача2");
            task2.setDescriptionTask("Обновленное описание задачи2");
            task2.setStatus(TaskStatus.IN_PROGRESS);
            manager.updateTask(task2); // Обновляем существующую задачу
        }

        if (task3.getId() != 0) {
            task3.setNameTask("Обновленная Задача3");
            task3.setDescriptionTask("Обновленное описание задачи3");
            task3.setStatus(TaskStatus.DONE);
            manager.updateTask(task3); // Обновляем существующую задачу
        }

        Epic epic3 = new Epic("Новый Эпик1", "Описание нового эпика1");
        manager.updateEpic(epic1.getId(), epic3);

        Subtask subtask5 = new Subtask("Подзадача новая2");
        subtask5.setDescriptionTask("Описание новой подзадачи");
        subtask5.setStatus(TaskStatus.IN_PROGRESS);
        subtask5.setEpicId(epic1.getId());

        int subtaskIdToUpdate = subtask2.getId();
        manager.updateSubtask(subtaskIdToUpdate, subtask5);

        manager.updateSubtask(subtask1.getId(), subtask1);
        manager.updateSubtask(subtask2.getId(), subtask2);

        System.out.println("Получить список всех задач после обновления.");
        printTasks(manager);
        printEpic(manager);
        printSubtasks(manager.getEpicSubtasks(epic1.getId()));
        System.out.println();

        System.out.println("Обновить статус для задач");
        manager.removeTaskById(task1.getId());
        manager.removeSubtaskById(subtask3.getId());
        manager.removeEpicById(epic1.getId());

        System.out.println("Получен список всех задач после удаления по ID.");
        printTasks(manager);
        printEpic(manager);
        printSubtasks(manager.getSubtasks());
        System.out.println();

        manager.removeAllTasks();
        manager.removeAllEpics();
        System.out.println("Список задач после удаления по ID");
        printTasks(manager);
        System.out.println();

        System.out.println("Подзадачи для " + epic1.getNameTask() + ":");
        List<Subtask> epic1Subtasks = manager.getEpicSubtasks(epic1.getId());
        printSubtasks(epic1Subtasks);

        System.out.println("Подзадачи для " + epic2.getNameTask() + ":");
        List<Subtask> epic2Subtasks = manager.getEpicSubtasks(epic2.getId());
        printSubtasks(epic2Subtasks);
    }

    public static void printTasks(TaskManager manager) {
        if (manager.getTasks().isEmpty()) {
            System.out.println("Список задач пуст");
        } else {
            for (int i = 0; i < manager.getTasks().size(); i++) {
                Task task = manager.getTasks().get(i);
                System.out.println((i + 1) + ". " + task.getNameTask() + " - Статус: " + task.getStatus().getDescription());
            }
        }
    }

    public static void printEpic(TaskManager manager) {
        if (manager.getEpics().isEmpty()) {
            System.out.println("Список эпиков пуст");
        } else {
            for (int i = 0; i < manager.getEpics().size(); i++) {
                Epic epic = manager.getEpics().get(i);
                System.out.println((i + 1) + ". " + epic.getNameTask());
            }
        }
    }

    public static void printSubtasks(List<Subtask> subtasks) {
        if (subtasks.isEmpty()) {
            System.out.println("Список подзадач пуст");
        } else {
            for (int i = 0; i < subtasks.size(); i++) {
                Subtask subtask = subtasks.get(i);
                System.out.println((i + 1) + ". " + subtask.getNameTask() + " - Статус: " + subtask.getStatus().getDescription());
            }
        }
    }
}

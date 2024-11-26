package com.yandex.tracker.model;

import static org.junit.jupiter.api.Assertions.*;

import com.yandex.tracker.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;

class EpicTest {

    private TaskManager taskManager;
    private HistoryManager historyManager;

    @BeforeEach
    void setAp() {
        taskManager = new InMemoryTaskManager(historyManager);
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void testTaskEqualityById() {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.NEW);

        task1.setId(1);
        task2.setId(1);

        assertEquals(task1, task2, "Задачи должны быть равны по id.");
    }

    @Test
    void testSubtaskEqualityById() {
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", TaskStatus.NEW, 1);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", TaskStatus.NEW, 1);

        subtask1.setId(1);
        subtask2.setId(1);

        assertEquals(subtask1, subtask2, "Подзадачи должны быть равны по id.");
    }

    @Test
    void testEpicCannotAddItselfAsSubtask() {
        Epic epic = new Epic("Epic 1", "Epic description");
        Subtask subtask = new Subtask("Subtask 1", "Subtask description", TaskStatus.NEW, epic.getId());

        List<Subtask> subtasks = new ArrayList<>();
        subtasks.add(subtask);

        assertThrows(IllegalArgumentException.class, () -> {
            epic.setSubtasks(subtasks);
        }, "Эпик не может добавлять себя в качестве подзадачи.");
    }

    @Test
    void testSubtaskCannotBeItsOwnEpic() {
        Subtask subtask = new Subtask("Subtask 1", "Subtask description", TaskStatus.NEW, 1);
        subtask.setId(1);

        assertThrows(IllegalArgumentException.class, () -> {
            subtask.setEpicId(subtask.getId());
        }, "Подзадача не может быть своим собственным эпиком.");
    }

    @Test
    void testGetTaskManagerReturnsInitializedInstance() {
        TaskManager manager1 = new InMemoryTaskManager(historyManager);
        TaskManager manager2 = new InMemoryTaskManager(historyManager);

        assertNotNull(manager1, "Менеджер задач не должен быть null.");
        assertSame(manager1, manager2, "Должны возвращаться один и тот же экземпляр менеджера задач.");
    }

    @Test
    void testGetHistoryManagerReturnsInitializedInstance() {
        HistoryManager history1 = new InMemoryHistoryManager();
        HistoryManager history2 = new InMemoryHistoryManager();

        assertNotNull(history1, "Менеджер истории не должен быть null.");
        assertSame(history1, history2, "Должны возвращаться один и тот же экземпляр менеджера истории.");
    }

    @Test
    void testInMemoryTaskManagerAddDifferentTasks() {
        InMemoryTaskManager manager = new InMemoryTaskManager(historyManager);

        Task task = new Task("Task", "Description", TaskStatus.NEW);
        Epic epic = new Epic("Epic", "Description");
        Subtask subtask = new Subtask("Subtask", "Description", TaskStatus.NEW, epic.getId());

        manager.createTask(task);
        manager.createEpic(epic);
        manager.createSubtask(subtask);

        assertEquals(1, manager.getTasks().size(), "Не удалось добавить задачу.");
        assertEquals(1, manager.getEpics().size(), "Не удалось добавить эпик.");
        assertEquals(1, manager.getSubtasks().size(), "Не удалось добавить подзадачу.");
    }

    @Test
    void testTaskIdConflict() {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.NEW);

        task1.setId(1);
        task2.setId(1);

        taskManager.createTask(task1);

        assertThrows(IllegalArgumentException.class, () -> {
            taskManager.createTask(task2);
        }, "Задачи с одинаковым id не должны добавляться.");
    }

    @Test
    void testTaskImmutabilityOnAdd() {
        Task originalTask = new Task("Original Task", "Description", TaskStatus.NEW);
        int id = taskManager.createTask(originalTask);
        Task fetchedTask = taskManager.getTaskById(id);

        fetchedTask.setNameTask("Modified Task");

        Task reFetchedTask = taskManager.getTaskById(id);

        assertEquals("Original Task", reFetchedTask.getNameTask(),
                "Задача должна оставаться неизменной после добавления в менеджер.");
    }

    @Test
    void testHistoryManagerPreservesPreviousVersion() {
        Task task = new Task("Task", "Description", TaskStatus.NEW);
        int id = taskManager.createTask(task);

        historyManager.add(taskManager.getTaskById(id));
    }
}
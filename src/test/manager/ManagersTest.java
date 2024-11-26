package test.manager;

import com.yandex.tracker.model.Epic;
import com.yandex.tracker.model.Subtask;
import com.yandex.tracker.model.Task;
import com.yandex.tracker.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ManagersTest {

    private TaskManager taskManager;
    private HistoryManager historyManager;

    @BeforeEach
    void setAp() {
        taskManager = new InMemoryTaskManager(historyManager);
        historyManager = new InMemoryHistoryManager();
    }

    // Тестирование добавления подзадачи к эпикам
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

    // Тестирование создания экземпляров менеджера задач
    @Test
    void testGetTaskManagerReturnsInitializedInstance() {
        TaskManager manager1 = new InMemoryTaskManager(historyManager);
        TaskManager manager2 = new InMemoryTaskManager(historyManager);

        assertNotNull(manager1, "Менеджер задач не должен быть null.");
        assertNotSame(manager1, manager2, "Должны возвращаться разные экземпляры менеджера задач.");
    }

    // Тестирование создания экземпляров менеджера истории
    @Test
    void testGetHistoryManagerReturnsInitializedInstance() {
        HistoryManager history1 = new InMemoryHistoryManager();
        HistoryManager history2 = new InMemoryHistoryManager();

        assertNotNull(history1, "Менеджер истории не должен быть null.");
        assertNotSame(history1, history2, "Должны возвращаться разные экземпляры менеджера истории.");
    }

    // Тестирование добавления различных задач в менеджер
    @Test
    void testInMemoryTaskManagerAddDifferentTasks() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Task task = new Task("Task 1", "Description 1", TaskStatus.NEW);
        int taskId = manager.createTask(task);
        assertEquals(task, manager.getTaskById(taskId));

        Epic epic = new Epic("Epic 1", "Description 1");
        int epicId = manager.createEpic(epic);
        assertEquals(epic, manager.getEpicById(epicId));

        Subtask subtask = new Subtask("Subtask 1", "Description 1", TaskStatus.NEW, epicId);
        int subtaskId = manager.createSubtask(subtask);
        assertEquals(subtask, manager.getSubtaskById(subtaskId));
    }

    // Тестирование конфликта ID задач
    @Test
    void testTaskIdConflict() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        int taskId1 = manager.createTask(task1);
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.NEW);
        int taskId2 = manager.createTask(task2);
        assertNotEquals(taskId1, taskId2);
    }

    // Тестирование удаления подзадачи и обновления связанного эпика
    @Test
    void testDeleteSubtaskUpdatesEpic() {
        Epic epic = new Epic("Epic 1", "Description 1");
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", TaskStatus.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", TaskStatus.NEW, epic.getId());

        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);

        int subtask1Id = subtask1.getId();
        epic.removeSubtask(subtask1Id);
        List<Subtask> remainingSubtasks = epic.getSubtasks();

        assertEquals(1, remainingSubtasks.size(), "Epic should have 1 subtask remaining.");
        assertFalse(remainingSubtasks.stream().anyMatch(subtask -> subtask.getId() == subtask1Id),
                "Epic should not contain subtask ID " + subtask1Id + " after removal.");
        assertTrue(remainingSubtasks.stream().anyMatch(subtask -> subtask.getId() == subtask2.getId()),
                "Epic should still contain subtask ID " + subtask2.getId() + ".");
    }

    // Тестирование целостности данных после удаления подзадачи
    @Test
    void testEpicDoesNotContainOldSubtaskIds() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Epic epic = new Epic("Epic 1", "Description for epic");
        manager.createEpic(epic);
        int epicId = epic.getId();
        Subtask subtask = new Subtask("Subtask 1", "Subtask Description", TaskStatus.NEW, epicId);
        int subtaskId = manager.createSubtask(subtask);
        manager.removeSubtaskById(subtaskId);
    }

    // Тестирование обновления статуса задачи
    @Test
    void testUpdateTaskStatus() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Task task = new Task("Task 1", "Description 1", TaskStatus.NEW);
        int taskId = manager.createTask(task);

        task.setStatus(TaskStatus.DONE);
        manager.updateTask(task);

        assertEquals(TaskStatus.DONE, manager.getTaskById(taskId).getStatus(),
                "Статус задачи должен обновиться в менеджере.");
    }

    // Тестирование сохранения истории задач
    @Test
    void testHistoryManagerPreservesPreviousVersion() {
        InMemoryHistoryManager historyManager = new InMemoryHistoryManager();
        Task task = new Task("Task 1", "Description 1", TaskStatus.NEW);
        historyManager.add(task);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());

        task.setStatus(TaskStatus.DONE);
        historyManager.add(task);
        history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(TaskStatus.DONE, history.get(0).getStatus());
    }

    // Тестирование изменения ID эпика подзадачи
    @Test
    void testUpdateSubtaskChangesEpicSubtaskIds() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Epic epic = new Epic("Epic 1", "Description for Epic 1");
        int epicId = manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask 1", "Description for Subtask 1", TaskStatus.NEW, epicId);
        int subtaskId = manager.createSubtask(subtask);
        epic.addSubtask(subtask);
        assertTrue(epic.getSubtasks().contains(subtask));
        int newSubtaskId = 2;
        subtask.setId(newSubtaskId);

        assertEquals(newSubtaskId, subtask.getId());
        assertTrue(epic.getSubtasks().stream().anyMatch(s -> s.getId() == newSubtaskId));
    }

    // Тестирование целостности данных при обновлении задачи
    @Test
    void testSettersDoNotAffectManagerData() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Task task = new Task("Task 1", "Description 1", TaskStatus.NEW);
        int taskId = manager.createTask(task);

        Task modifiedTask = new Task(task.getNameTask(), "Modified Description", task.getStatus());

        Task retrievedTask = manager.getTaskById(taskId);
        assertNotEquals("Modified Description", retrievedTask.getDescriptionTask(),
                "Manager data should not be affected by direct changes in task properties");
    }
}

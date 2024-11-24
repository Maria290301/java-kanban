package Test.manager;

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

    // Тестирование того, что подзадача не может быть своим собственным эпиком
    @Test
    void testSubtaskCannotBeItsOwnEpic() {
        Subtask subtask = new Subtask("Subtask 1", "Subtask description", TaskStatus.NEW, 1);
        subtask.setId(1);

        assertThrows(IllegalArgumentException.class, () -> {
            subtask.setEpicId(subtask.getId());
        }, "Подзадача не может быть своим собственным эпиком.");
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

    // Тестирование конфликта ID задач
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

    // Тестирование неизменности задачи при добавлении
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

    // Тестирование удаления подзадачи и обновления связанного эпика
    @Test
    void testDeleteSubtaskUpdatesEpic() {
        Epic epic = new Epic("Epic 1", "Epic Description");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask 1", "Subtask Description", TaskStatus.NEW, epicId);
        int subtaskId = taskManager.createSubtask(subtask);

        taskManager.removeSubtaskById(subtaskId);

        assertFalse(taskManager.getEpicById(epicId).getSubtasks().contains(subtask),
                "Эпик не должен содержать ID удаленной подзадачи.");
        assertEquals(0, taskManager.getEpicSubtasks(epicId).size(),
                "Эпик должен иметь 0 подзадач после удаления.");
    }

    // Тестирование целостности данных после удаления подзадачи
    @Test
    void testEpicDoesNotRetainOldSubtaskIds() {
        Epic epic = new Epic("Epic 1", "Epic Description");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask 1", "Subtask Description", TaskStatus.NEW, epicId);
        int subtaskId = taskManager.createSubtask(subtask);

        taskManager.removeSubtaskById(subtaskId);

        assertFalse(taskManager.getEpicById(epicId).getSubtasks().contains(subtask),
                "Эпик не должен содержать ID удаленной подзадачи.");
    }

    // Тестирование обновления статуса задачи
    @Test
    void testUpdateTaskStatus() {
        Task task = new Task("Task 1", "Description 1", TaskStatus.NEW);
        int taskId = taskManager.createTask(task);

        task.setStatus(TaskStatus.DONE);
        taskManager.updateTask(task);

        assertEquals(TaskStatus.DONE, taskManager.getTaskById(taskId).getStatus(),
                "Статус задачи должен обновиться в менеджере.");
    }

    // Тестирование сохранения истории задач
    @Test
    void testHistoryManagerPreservesPreviousVersion() {
        Task task = new Task("Task 1", "Description 1", TaskStatus.NEW);
        int taskId = taskManager.createTask(task);
        taskManager.getTaskById(taskId);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать одну задачу.");
        assertEquals(task, history.get(0), "Задача в истории должна совпадать с добавленной.");
    }

    // Тестирование изменения ID эпика подзадачи
    @Test
    void testUpdateSubtaskChangesEpicSubtaskIds() {
        Epic epic = new Epic("Epic 1", "Epic Description");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask 1", "Subtask Description", TaskStatus.NEW, epicId);
        int subtaskId = taskManager.createSubtask(subtask);

        subtask.setEpicId(999);
        taskManager.updateSubtask(subtask);

        assertFalse(taskManager.getEpicById(epicId).getSubtasks().contains(subtask),
                "Эпик не должен содержать ID подзадачи после изменения ее эпика.");
    }

    // Тестирование целостности данных при обновлении задачи
    @Test
    void testTaskSetterDoesNotAffectHistory() {
        Task task = new Task("Task 1", "Description 1", TaskStatus.NEW);
        int taskId = taskManager.createTask(task);

        task.setNameTask("Updated Task");
        taskManager.updateTask(task);

        assertEquals("Updated Task", taskManager.getTaskById(taskId).getNameTask(),
                "Имя задачи в менеджере должно обновиться.");
        assertNotEquals("Task 1", taskManager.getTaskById(taskId).getNameTask(),
                "Имя задачи в истории не должно измениться.");
    }

    // Тестирование корректности работы связного списка версий
    @Test
    void testVersioningSystem() {
        Task task = new Task("Task 1", "Description 1", TaskStatus.NEW);
        int taskId = taskManager.createTask(task);

        Task originalTask = taskManager.getTaskById(taskId);
        task.setDescriptionTask("Updated Description");
        taskManager.updateTask(task);
        Task updatedTask = taskManager.getTaskById(taskId);

        assertNotNull(originalTask, "Оригинальная задача не должна быть null.");
        assertNotNull(updatedTask, "Обновленная задача не должна быть null.");
        assertEquals("Description 1", originalTask.getDescriptionTask(),
                "Первая версия задачи должна иметь старое описание.");
        assertEquals("Updated Description", updatedTask.getDescriptionTask(),
                "Вторая версия задачи должна иметь новое описание.");
    }
}
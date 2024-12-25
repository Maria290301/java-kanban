package manager;

import com.yandex.tracker.model.Epic;
import com.yandex.tracker.model.Subtask;
import com.yandex.tracker.model.Task;
import com.yandex.tracker.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ManagersTest {

    private TaskManager taskManager;
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
        taskManager = new InMemoryTaskManager(historyManager);
    }

    // Тестирование добавления подзадачи к эпикам
    @Test
    void testEpicCannotAddItselfAsSubtask() {
        Epic epic = new Epic(1, "Epic 1", "Epic description", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now());
        Subtask subtask = new Subtask(1, "Subtask 1", "Subtask description", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now(), epic.getId());

        List<Subtask> subtasks = new ArrayList<>();
        subtasks.add(subtask);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            epic.setSubtasks(subtasks);
        });

        assertEquals("Эпик не может добавлять себя в качестве подзадачи.", exception.getMessage());
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
        Task task = new Task(1, "Task 1", "Description 1", TaskStatus.NEW, TaskType.TASK,
                Duration.ofHours(1), LocalDateTime.now());
        int taskId = manager.createTask(task);
        assertEquals(task, manager.getTaskById(taskId));

        Epic epic = new Epic(2, "Epic 1", "Description 1", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now());
        int epicId = manager.createEpic(epic);
        assertEquals(epic, manager.getEpicById(epicId));

        Subtask subtask = new Subtask(3, "Subtask 1", "Description 1", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now(), epicId);
        int subtaskId = manager.createSubtask(subtask);
        assertEquals(subtask, manager.getSubtaskById(subtaskId));
    }

    // Тестирование конфликта ID задач
    @Test
    void testTaskIdConflict() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());

        Task task1 = new Task(0, "Task 1", "Description 1", TaskStatus.NEW, TaskType.TASK,
                Duration.ofHours(1), LocalDateTime.now());
        int taskId1 = manager.createTask(task1);

        Task task2 = new Task(taskId1, "Task 2", "Description 2", TaskStatus.NEW, TaskType.TASK,
                Duration.ofHours(1), LocalDateTime.now());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            manager.createTask(task2);
        });

        assertEquals("Задачи с одинаковым id не должны добавляться.", exception.getMessage());
    }


    // Тестирование удаления подзадачи и обновления связанного эпика
    @Test
    void testAddConflictingSubtask() {
        Epic epic = new Epic(1, "Epic 1", "Description 1", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now());

        Subtask subtask1 = new Subtask(1, "Subtask 1", "Description 1", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now(), epic.getId());
        epic.addSubtask(subtask1);

        Subtask subtask2 = new Subtask(2, "Subtask 2", "Description 2", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now(), epic.getId());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            epic.addSubtask(subtask2);
        });

        assertEquals("Подзадача пересекается с существующей подзадачей.", exception.getMessage());
    }

    // Тестирование целостности данных после удаления подзадачи
    @Test
    void testEpicDoesNotContainOldSubtaskIds() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Epic epic = new Epic(1, "Epic 1", "Description for epic", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now());
        manager.createEpic(epic);
        int epicId = epic.getId();
        Subtask subtask = new Subtask(2, "Subtask 1", "Subtask Description", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now(), epicId);
        int subtaskId = manager.createSubtask(subtask);
        manager.removeSubtaskById(subtaskId);
    }

    // Тестирование обновления статуса задачи
    @Test
    void testUpdateTaskStatus() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Task task = new Task(1, "Task 1", "Description 1", TaskStatus.NEW, TaskType.TASK,
                Duration.ofHours(1), LocalDateTime.now());
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
        Task task = new Task(1, "Task 1", "Description 1", TaskStatus.NEW, TaskType.TASK,
                Duration.ofHours(1), LocalDateTime.now());
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
    void testUpdateSubtaskChangesEpicSubtaskIds()  {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Epic epic = new Epic(1, "Epic 1", "Description for Epic 1", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now());
        int epicId = manager.createEpic(epic);

        Subtask subtask = new Subtask(1, "Subtask 1", "Description for Subtask 1",
                TaskStatus.NEW, Duration.ofHours(1), LocalDateTime.now(), epicId);
        manager.createSubtask(subtask);
        epic.addSubtask(subtask);

        Subtask conflictingSubtask = new Subtask(2, "Subtask 2", "Description for Subtask 2",
                TaskStatus.NEW, Duration.ofHours(1), LocalDateTime.now(), epicId);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            manager.createSubtask(conflictingSubtask);
        });

        assertEquals("Подзадача пересекается с существующей подзадачей.", exception.getMessage());
    }

    // Тестирование целостности данных при обновлении задачи
    @Test
    void testSettersDoNotAffectManagerData() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Task task = new Task(1, "Task 1", "Description 1", TaskStatus.NEW, TaskType.TASK,
                Duration.ofHours(1), LocalDateTime.now());
        int taskId = manager.createTask(task);

        Task modifiedTask = new Task(task.getId(), task.getNameTask(), "Modified Description",
                task.getStatus(), task.getTaskType(), task.getDuration(), task.getStartTime());

        Task retrievedTask = manager.getTaskById(taskId);
        assertNotEquals("Modified Description", retrievedTask.getDescriptionTask(),
                "Manager data should not be affected by direct changes in task properties");
    }

    // Тестирование создания задач с пересекающимся временем выполнения
    @Test
    public void testCreateTask_OverlappingTasks() {
        InMemoryTaskManager manager = new InMemoryTaskManager();
        Task task1 = new Task(1, "Task 1", "Description 1", TaskStatus.NEW, TaskType.TASK,
                Duration.ofHours(2), LocalDateTime.of(2023, 10, 1, 10, 0));
        Task task2 = new Task(2, "Task 2", "Description 2", TaskStatus.NEW, TaskType.TASK,
                Duration.ofHours(1), LocalDateTime.of(2023, 10, 1, 11, 0));

        manager.createTask(task1);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            manager.createTask(task2);
        });

        assertEquals("Задача пересекается с существующей задачей.", exception.getMessage());
    }
}


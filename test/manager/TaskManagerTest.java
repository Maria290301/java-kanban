package manager;

import com.yandex.tracker.model.Task;
import com.yandex.tracker.model.Epic;
import com.yandex.tracker.model.Subtask;
import com.yandex.tracker.service.TaskManager;
import com.yandex.tracker.service.TaskStatus;
import com.yandex.tracker.service.TaskType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;

    protected abstract T createManager();

    @BeforeEach
    public void setUp() {
        manager = createManager();
    }

    // Тестирование создания задачи
    @Test
    public void testCreateTask() {
        Task task = new Task(0, "Task #1", "Task1 description", TaskStatus.NEW, TaskType.TASK,
                Duration.ofMinutes(30), LocalDateTime.now());
        int taskId = manager.createTask(task);
        assertEquals(1, manager.getTasks().size());
        assertEquals(taskId, task.getId());
    }

    // Тестирование создания подзадачи с эпиком
    @Test
    public void testCreateSubtaskWithEpic() {
        Epic epic = new Epic(0, "Epic 1", "Epic description", TaskStatus.NEW,
                Duration.ZERO, LocalDateTime.now());
        manager.createEpic(epic);
        Subtask subtask1 = new Subtask(0, "Subtask 1", "Description 1", TaskStatus.NEW,
                Duration.ofMinutes(20), LocalDateTime.now(), epic.getId());
        int subtaskId = manager.createSubtask(subtask1);
        assertEquals(1, manager.getSubtasks().size());
        assertEquals(subtaskId, subtask1.getId());
        assertEquals(1, manager.getEpicSubtasks(epic.getId()).size());
    }

    // Тестирование расчета статуса эпика
    @Test
    public void testEpicStatusCalculation() {
        Epic epic = new Epic(0, "Epic 1", "Epic description", TaskStatus.NEW, Duration.ZERO,
                LocalDateTime.now());
        manager.createEpic(epic);
        Subtask subtask1 = new Subtask(0, "Subtask 1", "Description 1", TaskStatus.NEW,
                Duration.ofMinutes(20), LocalDateTime.now(), epic.getId());
        Subtask subtask2 = new Subtask(0, "Subtask 2", "Description 2", TaskStatus.NEW,
                Duration.ofMinutes(25), LocalDateTime.now(), epic.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        assertEquals(TaskStatus.NEW, epic.getStatus());

        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask1);
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    // Тестирование удаления задачи
    @Test
    public void testRemoveTask() {
        Task task = new Task(0, "Task #1", "Task1 description", TaskStatus.NEW, TaskType.TASK,
                Duration.ofMinutes(30), LocalDateTime.now());
        manager.createTask(task);
        manager.removeTaskById(task.getId());
        assertTrue(manager.getTasks().isEmpty());
    }

    // Тестирование удаления эпика
    @Test
    public void testRemoveEpic() {
        Epic epic = new Epic(0, "Epic 1", "Epic description", TaskStatus.NEW, Duration.ZERO,
                LocalDateTime.now());
        manager.createEpic(epic);
        Subtask subtask1 = new Subtask(0, "Subtask 1", "Description 1", TaskStatus.NEW,
                Duration.ofMinutes(20), LocalDateTime.now(), epic.getId());
        manager.createSubtask(subtask1);
        manager.removeEpicById(epic.getId());
        assertTrue(manager.getEpics().isEmpty());
        assertTrue(manager.getSubtasks().isEmpty());
    }

    // Тестирование пересечения задач по времени
    @Test
    public void testTaskOverlap() {
        Task task1 = new Task(0, "Task #1", "Task1 description", TaskStatus.NEW,
                TaskType.TASK, Duration.ofMinutes(30), LocalDateTime.of(2023, 10, 1, 10, 0));
        manager.createTask(task1);

        Task task2 = new Task(0, "Task #2", "Task2 description", TaskStatus.NEW,
                TaskType.TASK, Duration.ofMinutes(30), LocalDateTime.of(2023, 10, 1, 10, 15));

        assertThrows(IllegalArgumentException.class, () -> manager.createTask(task2), "Задачи пересекаются по времени.");
    }
    @Test
    public void testAddTask() {
        Task task = new Task(0, "Task 1", "Description 1", TaskStatus.NEW, TaskType.TASK,
                Duration.ofHours(1), LocalDateTime.now());
        int taskId = manager.createTask(task);

        assertEquals(1, manager.getPrioritizedTasks().size());
        assertEquals(task, manager.getPrioritizedTasks().get(0));
        assertEquals(taskId, task.getId());
    }

    @Test
    public void testRemoveTaskById() {
        Task task = new Task(0, "Task 1", "Description 1", TaskStatus.NEW, TaskType.TASK,
                Duration.ofHours(1), LocalDateTime.now());
        int taskId = manager.createTask(task);

        manager.removeTaskById(taskId);

        assertEquals(0, manager.getPrioritizedTasks().size());
    }

    @Test
    public void testRemoveAllTasks() {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task(0, "Task 1", "Description 1", TaskStatus.NEW, TaskType.TASK,
                Duration.ofHours(1), now);
        Task task2 = new Task(0, "Task 2", "Description 2", TaskStatus.NEW, TaskType.TASK,
                Duration.ofHours(1), now.plusHours(2));

        manager.createTask(task1);
        manager.createTask(task2);

        manager.removeTasks();

        assertEquals(0, manager.getPrioritizedTasks().size());
    }

    @Test
    public void testUpdateTask() {
        Task task = new Task(0, "Task 1", "Description 1", TaskStatus.NEW, TaskType.TASK,
                Duration.ofHours(1), LocalDateTime.now());
        int taskId = manager.createTask(task);

        Task updatedTask = new Task(taskId, "Updated Task", "Updated Description",
                TaskStatus.IN_PROGRESS, TaskType.TASK, Duration.ofHours(2), LocalDateTime.now().plusHours(1));
        manager.updateTask(updatedTask);

        assertEquals(1, manager.getPrioritizedTasks().size());
        assertEquals(updatedTask, manager.getPrioritizedTasks().get(0));
    }

    @Test
    public void testAddSubtask() {
        Epic epic = new Epic(0, "Epic 1", "Epic Description", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now());
        int epicId = manager.createEpic(epic);

        Subtask subtask = new Subtask(0, "Subtask 1", "Subtask Description", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now(), epicId);
        int subtaskId = manager.createSubtask(subtask);

        assertEquals(1, manager.getEpicSubtasks(epicId).size());
        assertEquals(subtask, manager.getEpicSubtasks(epicId).get(0));
        assertEquals(subtaskId, subtask.getId());
    }

    @Test
    public void testRemoveSubtaskById() {
        Epic epic = new Epic(0, "Epic 1", "Epic Description", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now());
        int epicId = manager.createEpic(epic);

        Subtask subtask = new Subtask(0, "Subtask 1", "Subtask Description", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now(), epicId);
        int subtaskId = manager.createSubtask(subtask);

        manager.removeSubtaskById(subtaskId);

        assertEquals(0, manager.getEpicSubtasks(epicId).size());
    }

    @Test
    public void testRemoveAllSubtasks() {
        Epic epic = new Epic(0, "Epic 1", "Epic Description", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now());
        int epicId = manager.createEpic(epic);

        // Установите разные временные интервалы для подзадач
        LocalDateTime now = LocalDateTime.now();
        Subtask subtask1 = new Subtask(0, "Subtask 1", "Subtask Description", TaskStatus.NEW,
                Duration.ofHours(1), now, epicId);
        Subtask subtask2 = new Subtask(0, "Subtask 2", "Subtask Description", TaskStatus.NEW,
                Duration.ofHours(1), now.plusHours(2), epicId);

        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        manager.removeSubtasks();

        assertEquals(0, manager.getEpicSubtasks(epicId).size());
    }

    @Test
    public void testUpdateSubtask() {
        Epic epic = new Epic(0, "Epic 1", "Epic Description", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now());
        int epicId = manager.createEpic(epic);

        Subtask subtask = new Subtask(0, "Subtask 1", "Subtask Description", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now(), epicId);
        int subtaskId = manager.createSubtask(subtask);

        Subtask updatedSubtask = new Subtask(subtaskId, "Updated Subtask", "Updated Description",
                TaskStatus.IN_PROGRESS, Duration.ofHours(2), LocalDateTime.now().plusHours(1), epicId);
        manager.updateSubtask(updatedSubtask);

        assertEquals(1, manager.getEpicSubtasks(epicId).size());
        assertEquals(updatedSubtask, manager.getEpicSubtasks(epicId).get(0));
    }
}

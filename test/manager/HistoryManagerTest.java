package manager;

import com.yandex.tracker.model.Task;
import com.yandex.tracker.service.HistoryManager;
import com.yandex.tracker.service.InMemoryHistoryManager;
import com.yandex.tracker.service.TaskStatus;
import com.yandex.tracker.service.TaskType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    public void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    // Тестирование пустой истории: проверка, что история пуста при инициализации
    @Test
    public void testEmptyHistory() {
        assertTrue(historyManager.getHistory().isEmpty());
    }

    // Тестирование добавления задачи в историю: проверка, что размер истории увеличивается на 1 после добавления задачи
    @Test
    public void testAddToHistory() {
        Task task = new Task(1, "Task 1", "Description 1", TaskStatus.NEW, TaskType.TASK,
                Duration.ofHours(1), LocalDateTime.now());
        historyManager.add(task);
        assertEquals(1, historyManager.getHistory().size());
    }

    // Тестирование добавления дубликатов в историю: проверка, что дубликаты не увеличивают размер истории
    @Test
    public void testDuplicateHistory() {
        Task task = new Task(1, "Task 1", "Description 1", TaskStatus.NEW, TaskType.TASK,
                Duration.ofHours(1), LocalDateTime.now());
        historyManager.add(task);
        historyManager.add(task);
        assertEquals(1, historyManager.getHistory().size());
    }

    // Тестирование удаления задачи из истории: проверка, что размер истории уменьшается при удалении задачи
    @Test
    public void testRemoveFromHistory() {
        Task task1 = new Task(1, "Task 1", "Description 1", TaskStatus.NEW, TaskType.TASK,
                Duration.ofHours(1), LocalDateTime.now());
        Task task2 = new Task(2, "Task 2", "Description 2", TaskStatus.NEW, TaskType.TASK,
                Duration.ofHours(1), LocalDateTime.now());
        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(1);
        assertEquals(1, historyManager.getHistory().size());

        historyManager.remove(2);
        assertTrue(historyManager.getHistory().isEmpty());
    }
}

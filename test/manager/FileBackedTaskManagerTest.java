package manager;

import com.yandex.tracker.model.Task;
import com.yandex.tracker.service.FileBackedTaskManager;
import com.yandex.tracker.service.InMemoryHistoryManager;
import com.yandex.tracker.exception.ManagerSaveException;
import com.yandex.tracker.service.TaskStatus;
import com.yandex.tracker.service.TaskType;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {
    private FileBackedTaskManager manager;
    private File tempFile;

    @BeforeEach
    public void setUp() throws IOException {
        tempFile = File.createTempFile("tempTasks", ".csv");
        tempFile.deleteOnExit();
        manager = new FileBackedTaskManager(tempFile);
    }

    // Тестирование сохранения и загрузки пустого файла
    @Test
    public void testSaveAndLoadEmptyFile() {
        assertTrue(tempFile.length() == 0);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile, new InMemoryHistoryManager());

        assertTrue(loadedManager.getTasks().isEmpty());
        assertTrue(loadedManager.getEpics().isEmpty());
        assertTrue(loadedManager.getSubtasks().isEmpty());
    }

    // Тестирование сохранения нескольких задач
    @Test
    public void testSaveMultipleTasks() {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task(1, "Task 1", "Description 1", TaskStatus.NEW,
                TaskType.TASK, Duration.ofHours(1), now);
        Task task2 = new Task(2, "Task 2", "Description 2", TaskStatus.IN_PROGRESS,
                TaskType.TASK, Duration.ofHours(1), now.plusHours(2));

        manager.createTask(task1);
        manager.createTask(task2);
        manager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile, new InMemoryHistoryManager());
        assertEquals(2, loadedManager.getTasks().size());
        assertEquals(task1.getNameTask(), loadedManager.getTasks().get(0).getNameTask());
        assertEquals(task2.getNameTask(), loadedManager.getTasks().get(1).getNameTask());
    }


    // Тестирование загрузки нескольких задач в файл напрямую
    @Test
    public void testLoadMultipleTasks() throws IOException {
        String content = "id,type,name,status,description,duration,startTime,epic\n" +
                "1,TASK,Task 1,NEW,Description 1,P1D,2023-10-01T10:00:00,0\n" +
                "2,TASK,Task 2,IN_PROGRESS,Description 2,P2D,2023-10-02T11:00:00,0\n" +
                "1,TASK,Task 1 Duplicate,NEW,Description 1 Duplicate,P1D,2023-10-01T10:00:00,0\n";

        Files.writeString(tempFile.toPath(), content);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            FileBackedTaskManager.loadFromFile(tempFile, new InMemoryHistoryManager());
        });

        assertEquals("Задача с таким идентификатором уже существует", thrown.getMessage());
    }

    // Тестирование обработки ошибок
    @Test
    public void testLoadFromNonExistentFile() {
        File nonExistentFile = new File("test/resources/non_existent_file.csv");

        if (nonExistentFile.exists()) {
            nonExistentFile.delete();
        }

        assertThrows(ManagerSaveException.class, () -> {
            FileBackedTaskManager.loadFromFile(nonExistentFile, new InMemoryHistoryManager());
        });
    }

    // Тестирование обработки пустых строк
    @Test
    public void testLoadWithEmptyLines() throws IOException {
        String content = "id,type,name,status,description,duration,startTime,epic\n" +
                "1,TASK,Task 1,NEW,Description 1,P1D,2023-10-01T10:00:00,0\n" +
                "\n" +
                "2,TASK,Task 2,IN_PROGRESS,Description 2,P2D,2023-10-02T11:00:00,0\n";
        Files.writeString(tempFile.toPath(), content);
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile, new InMemoryHistoryManager());
        assertEquals(2, loadedManager.getTasks().size());
    }

    // Тестирование исключения при операциях с файлами
    @Test
    public void testExceptionOnFileOperation() {
        String nonExistentPath = "/some/non/existent/path/test.csv";

        assertThrows(ManagerSaveException.class, () -> {
            new FileBackedTaskManager(new File(nonExistentPath));
        });
    }
}
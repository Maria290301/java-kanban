package manager;

import com.yandex.tracker.model.Task;
import com.yandex.tracker.service.FileBackedTaskManager;
import com.yandex.tracker.service.InMemoryHistoryManager;
import com.yandex.tracker.service.TaskStatus;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {
    private FileBackedTaskManager manager;
    private File tempFile;

    @BeforeEach
    public void setUp() throws IOException {
        tempFile = File.createTempFile("tempTasks", ".csv");
        tempFile.deleteOnExit();
        manager = new FileBackedTaskManager(new InMemoryHistoryManager(), tempFile);
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
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.IN_PROGRESS);
        manager.createTask(task1);
        manager.createTask(task2);

        assertEquals(2, manager.getTasks().size());

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile, new InMemoryHistoryManager());

        assertEquals(2, loadedManager.getTasks().size());
        assertEquals(task1.getNameTask(), loadedManager.getTasks().get(0).getNameTask());
        assertEquals(task2.getNameTask(), loadedManager.getTasks().get(1).getNameTask());
    }

    // Тестирование загрузки нескольких задач в файл напрямую
    @Test
    public void testLoadMultipleTasks() throws IOException {
        String content = "id,type,name,status,description,epic\n" +
                "1,TASK,Task 1,NEW,Description 1,0\n" +
                "2,TASK,Task 2,IN_PROGRESS,Description 2,0\n" +
                "1,TASK,Duplicate Task 1,NEW,Description 1,0\n";

        Files.writeString(tempFile.toPath(), content);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            FileBackedTaskManager.loadFromFile(tempFile, new InMemoryHistoryManager());
        });

        assertEquals("Задача с таким идентификатором уже существует", thrown.getMessage());

        FileBackedTaskManager emptyManager = new FileBackedTaskManager(new InMemoryHistoryManager(), tempFile);
        assertEquals(0, emptyManager.getTasks().size());
    }

    // Тестирование обработки ошибок
    @Test
    public void testLoadFromNonExistentFile() {
        File nonExistentFile = new File("non_existent_file.csv");
        assertThrows(FileBackedTaskManager.ManagerSaveException.class, () -> {
            FileBackedTaskManager.loadFromFile(nonExistentFile, new InMemoryHistoryManager());
        });
    }

    // Тестирование обработки пустых строк
    @Test
    public void testLoadWithEmptyLines() throws IOException {
        String content = "id,type,name,status,description,epic\n" +
                "1,TASK,Task 1,NEW,Description 1,0\n" +
                "\n" +
                "2,TASK,Task 2,IN_PROGRESS,Description 2,0\n";
        Files.writeString(tempFile.toPath(), content);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile, new InMemoryHistoryManager());

        assertEquals(2, loadedManager.getTasks().size());
        assertEquals("Task 1", loadedManager.getTasks().get(0).getNameTask());
        assertEquals("Task 2", loadedManager.getTasks().get(1).getNameTask());
    }
}

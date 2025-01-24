package http;

import com.google.gson.Gson;
import com.yandex.tracker.http.HttpTaskServer;
import com.yandex.tracker.model.Epic;
import com.yandex.tracker.model.Subtask;
import com.yandex.tracker.model.Task;
import com.yandex.tracker.service.InMemoryTaskManager;
import com.yandex.tracker.service.TaskManager;
import com.yandex.tracker.service.TaskStatus;
import com.yandex.tracker.service.TaskType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTasksTest {
    private final TaskManager manager = new InMemoryTaskManager();
    private final HttpTaskServer taskServer;
    private static final Gson gson = HttpTaskServer.getGson();

    public HttpTaskManagerTasksTest() throws IOException {
        taskServer = new HttpTaskServer(manager);
    }

    @BeforeEach
    public void setUp() throws IOException {
        manager.removeTasks();
        manager.removeSubtasks();
        manager.removeEpics();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testAddTask() throws IOException {
        Task task = new Task(1, "Task 1", "Testing task creation", TaskStatus.NEW,
                TaskType.TASK, Duration.ofHours(1), LocalDateTime.now());
        String jsonTask = gson.toJson(task);

        URL url = new URL("http://localhost:8080/tasks");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");

        try (var outputStream = connection.getOutputStream()) {
            byte[] input = jsonTask.getBytes(StandardCharsets.UTF_8);
            outputStream.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        assertEquals(200, responseCode);
    }


    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        Task task = new Task(1, "Test Task", "Testing task creation", TaskStatus.NEW,
                TaskType.TASK, Duration.ofMinutes(5), LocalDateTime.now());
        manager.createTask(task);

        task.setNameTask("Updated Task");
        String taskJson = gson.toJson(task);
        System.out.println("Task JSON: " + taskJson);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + task.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> tasksFromManager = manager.getTasks();
        assertEquals("Updated Task", tasksFromManager.get(0).getNameTask());
    }

    @Test
    public void testDeleteTask() throws IOException, InterruptedException {
        Task task = new Task(1, "Test Task", "Testing task deletion", TaskStatus.NEW,
                TaskType.TASK, Duration.ofMinutes(5), LocalDateTime.now());
        manager.createTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + task.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> tasksFromManager = manager.getTasks();
        assertEquals(0, tasksFromManager.size());
    }

    @Test
    public void testAddSubtask() throws IOException {
        Epic epic = new Epic(0, "Epic Task", "Testing epic creation", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now());
        manager.createEpic(epic);

        Subtask subtask = new Subtask(0, "Subtask 1", "Testing subtask creation",
                TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.now(), epic.getId());
        String jsonSubtask = gson.toJson(subtask);

        URL url = new URL("http://localhost:8080/subtasks");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");

        try (var outputStream = connection.getOutputStream()) {
            byte[] input = jsonSubtask.getBytes(StandardCharsets.UTF_8);
            outputStream.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        assertEquals(200, responseCode);
    }

    @Test
    public void testUpdateSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic(1, "Epic Task", "Testing epic creation", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now());
        manager.createEpic(epic);

        Subtask subtask = new Subtask(1, "Subtask 1", "Testing subtask creation",
                TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.now(), epic.getId());
        manager.createSubtask(subtask);

        subtask.setNameTask("Updated Subtask");
        String subtaskJson = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subtask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).PUT(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Subtask> subtasksFromManager = manager.getSubtasks();
        assertEquals("Updated Subtask", subtasksFromManager.get(0).getNameTask());
    }

    @Test
    public void testDeleteSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic(1, "Epic Task", "Testing epic creation", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now());;
        manager.createEpic(epic);

        Subtask subtask = new Subtask(1, "Subtask 1", "Testing subtask creation",
                TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.now(), epic.getId());
        manager.createSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subtask.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Subtask> subtasksFromManager = manager.getSubtasks();
        assertEquals(0, subtasksFromManager.size());
    }

    @Test
    public void testAddEpic() throws IOException {
        Epic epic = new Epic(1, "Epic Task", "Testing epic creation", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now());
        String jsonEpic = gson.toJson(epic);

        URL url = new URL("http://localhost:8080/epics");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");

        try (var outputStream = connection.getOutputStream()) {
            byte[] input = jsonEpic.getBytes(StandardCharsets.UTF_8);
            outputStream.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        assertEquals(200, responseCode);
    }

    @Test
    public void testUpdateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic(1, "Epic Task", "Testing epic creation", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now());
        manager.createEpic(epic);

        epic.setNameTask("Updated Epic");
        String epicJson = gson.toJson(epic);
        System.out.println("Epic JSON: " + epicJson);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epic.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Epic> epicsFromManager = manager.getEpics();
        assertEquals("Updated Epic", epicsFromManager.get(0).getNameTask());
    }

    @Test
    public void testDeleteEpic() throws IOException, InterruptedException {
        Epic epic = new Epic(1, "Epic Task", "Testing epic creation", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.now());
        manager.createEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epic.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Epic> epicsFromManager = manager.getEpics();
        assertEquals(0, epicsFromManager.size());
    }
}


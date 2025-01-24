package com.yandex.tracker.http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.tracker.http.BaseHttpHandler;
import com.yandex.tracker.http.HttpTaskServer;
import com.yandex.tracker.model.Task;
import com.yandex.tracker.service.TaskManager;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import java.io.IOException;

import java.util.List;


public class TaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public TaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.gson = HttpTaskServer.getGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        switch (exchange.getRequestMethod()) {
            case "GET":
                handleGet(exchange);
                break;
            case "POST":
                handlePost(exchange);
                break;
            case "DELETE":
                handleDelete(exchange);
                break;
            case "PUT":
                handlePut(exchange);
                break;
            default:
                sendNotFound(exchange);
                break;
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        List<Task> tasks = taskManager.getTasks();
        String jsonResponse = gson.toJson(tasks);
        sendText(exchange, jsonResponse);
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        if (!"application/json".equals(exchange.getRequestHeaders().getFirst("Content-Type"))) {
            sendBadRequest(exchange);
            return;
        }
        try (Reader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            Task task = gson.fromJson(reader, Task.class);

            if (task.getNameTask() == null || task.getDescriptionTask() == null) {
                sendBadRequest(exchange);
                return;
            }
            taskManager.createTask(task);
            sendText(exchange, gson.toJson(task));
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange);
        } catch (Exception e) {
            e.printStackTrace();
            sendInternalError(exchange);
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String id = exchange.getRequestURI().getPath().split("/")[2];
        try {
            int taskId = Integer.parseInt(id);
            taskManager.removeTaskById(taskId);
            sendText(exchange, "Task deleted");
        } catch (NumberFormatException e) {
            sendBadRequest(exchange);
        } catch (Exception e) {
            e.printStackTrace();
            sendInternalError(exchange);
        }
    }

    private void handlePut(HttpExchange exchange) throws IOException {
        String id = exchange.getRequestURI().getPath().split("/")[2];
        if (!"application/json".equals(exchange.getRequestHeaders().getFirst("Content-Type"))) {
            sendBadRequest(exchange);
            return;
        }
        try (Reader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            Task task = gson.fromJson(reader, Task.class);
            if (task.getNameTask() == null || task.getDescriptionTask() == null) {
                sendBadRequest(exchange);
                return;
            }
            task.setId(Integer.parseInt(id));
            taskManager.updateTask(task);
            sendText(exchange, gson.toJson(task));
        } catch (NumberFormatException e) {
            sendBadRequest(exchange);
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange);
        } catch (Exception e) {
            e.printStackTrace();
            sendInternalError(exchange);
        }
    }
}

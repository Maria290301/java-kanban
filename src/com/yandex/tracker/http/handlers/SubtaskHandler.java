package com.yandex.tracker.http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.tracker.http.BaseHttpHandler;
import com.yandex.tracker.http.HttpTaskServer;
import com.yandex.tracker.model.Subtask;
import com.yandex.tracker.service.TaskManager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public SubtaskHandler(TaskManager taskManager) {
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
        List<Subtask> subtasks = taskManager.getSubtasks();
        String jsonResponse = gson.toJson(subtasks);
        sendText(exchange, jsonResponse);
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        if (!"application/json".equals(exchange.getRequestHeaders().getFirst("Content-Type"))) {
            sendBadRequest(exchange);
            return;
        }
        try (Reader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            Subtask subtask = gson.fromJson(reader, Subtask.class);

            if (subtask.getNameTask() == null || subtask.getDescriptionTask() == null || subtask.getEpicId() <= 0) {
                sendBadRequest(exchange);
                return;
            }

            taskManager.createSubtask(subtask);
            sendText(exchange, gson.toJson(subtask));
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
            int subtaskId = Integer.parseInt(id);
            taskManager.removeSubtaskById(subtaskId);
            sendText(exchange, "Subtask deleted");
        } catch (NumberFormatException e) {
            sendInternalError(exchange);
        }
    }

    private void handlePut(HttpExchange exchange) throws IOException {
        String id = exchange.getRequestURI().getPath().split("/")[2];
        try (Reader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            Subtask subtask = gson.fromJson(reader, Subtask.class);
            subtask.setId(Integer.parseInt(id));
            taskManager.updateSubtask(subtask);
            sendText(exchange, gson.toJson(subtask));
        } catch (NumberFormatException e) {
            sendInternalError(exchange);
        }
    }
}

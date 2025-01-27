package com.yandex.tracker.http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.tracker.http.BaseHttpHandler;
import com.yandex.tracker.http.HttpTaskServer;
import com.yandex.tracker.model.Epic;
import com.yandex.tracker.service.TaskManager;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import java.io.IOException;
import java.util.List;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson = HttpTaskServer.getGson();

    public EpicHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
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
        List<Epic> epics = taskManager.getEpics();
        String jsonResponse = gson.toJson(epics);
        sendText(exchange, jsonResponse);
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        if (!"application/json".equals(exchange.getRequestHeaders().getFirst("Content-Type"))) {
            sendBadRequest(exchange);
            return;
        }
        try (Reader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            Epic epic = gson.fromJson(reader, Epic.class);
            if (epic.getNameTask() == null || epic.getDescriptionTask() == null) {
                sendBadRequest(exchange);
                return;
            }
            taskManager.createEpic(epic);
            sendText(exchange, gson.toJson(epic));
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange);
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String id = exchange.getRequestURI().getPath().split("/")[2];
        try {
            int epicId = Integer.parseInt(id);
            taskManager.removeEpicById(epicId);
            sendText(exchange, "Epic deleted");
        } catch (NumberFormatException e) {
            sendBadRequest(exchange);
        }
    }

    private void handlePut(HttpExchange exchange) throws IOException {
        String id = exchange.getRequestURI().getPath().split("/")[2];
        if (!"application/json".equals(exchange.getRequestHeaders().getFirst("Content-Type"))) {
            sendBadRequest(exchange);
            return;
        }
        try (Reader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            Epic epic = gson.fromJson(reader, Epic.class);
            if (epic.getNameTask() == null || epic.getDescriptionTask() == null) {
                sendBadRequest(exchange);
                return;
            }
            epic.setId(Integer.parseInt(id));
            taskManager.updateEpic(epic);
            sendText(exchange, gson.toJson(epic));
        } catch (NumberFormatException | JsonSyntaxException e) {
            sendBadRequest(exchange);
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }
}

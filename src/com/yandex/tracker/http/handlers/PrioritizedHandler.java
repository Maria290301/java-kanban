package com.yandex.tracker.http.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.yandex.tracker.http.BaseHttpHandler;
import com.yandex.tracker.model.Task;
import com.yandex.tracker.service.TaskManager;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson = new Gson();

    public PrioritizedHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            handleGet(exchange);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        String jsonResponse = gson.toJson(prioritizedTasks);
        sendText(exchange, jsonResponse);
    }
}

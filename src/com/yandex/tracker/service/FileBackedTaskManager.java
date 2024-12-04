package com.yandex.tracker.service;

import com.yandex.tracker.model.Epic;
import com.yandex.tracker.model.Subtask;
import com.yandex.tracker.model.Task;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    @Override
    public int createTask(Task task) {
        if (getTasks().stream().anyMatch(t -> t.getId() == task.getId())) {
            throw new IllegalArgumentException("Задача с таким идентификатором уже существует");
        }
        int taskId = super.createTask(task);
        save();
        return taskId;
    }

    @Override
    public int createEpic(Epic epic) {
        if (getEpics().stream().anyMatch(e -> e.getId() == epic.getId())) {
            throw new IllegalArgumentException("Эпик с таким идентификатором уже существует");
        }
        int epicId = super.createEpic(epic);
        save();
        return epicId;
    }

    @Override
    public Integer createSubtask(Subtask subtask) {
        if (getSubtasks().stream().anyMatch(s -> s.getId() == subtask.getId())) {
            throw new IllegalArgumentException("Подзадача с таким идентификатором уже существует");
        }
        int subtaskId = super.createSubtask(subtask);
        save();
        return subtaskId;
    }

    @Override
    public void removeTasks() {
        super.removeTasks();
        save();
    }

    @Override
    public void removeEpics() {
        super.removeEpics();
        save();
    }

    @Override
    public void removeSubtasks() {
        super.removeSubtasks();
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void removeTaskById(int id) {
        super.removeTaskById(id);
        save();
    }

    @Override
    public void removeEpicById(int id) {
        super.removeEpicById(id);
        save();
    }

    @Override
    public void removeSubtaskById(int id) {
        super.removeSubtaskById(id);
        save();
    }

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,epic\n");
            for (Task task : getTasks()) {
                writer.write(toString(task) + "\n");
            }
            for (Epic epic : getEpics()) {
                writer.write(toString(epic) + "\n");
            }
            for (Subtask subtask : getSubtasks()) {
                writer.write(toString(subtask) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении данных в файл", e);
        }
    }

    public static String toString(Task task) {
        if (task instanceof Subtask subtask) {
            return String.format("%d,SUBTASK,%s,%s,%s,%d",
                    subtask.getId(),
                    subtask.getNameTask(),
                    subtask.getStatus(),
                    subtask.getDescriptionTask(),
                    subtask.getEpicId());
        } else if (task instanceof Epic epic) {
            return String.format("%d,EPIC,%s,%s,%s",
                    epic.getId(),
                    epic.getNameTask(),
                    epic.getStatus(),
                    epic.getDescriptionTask());
        } else if (task != null) {
            return String.format("%d,TASK,%s,%s,%s",
                    task.getId(),
                    task.getNameTask(),
                    task.getStatus(),
                    task.getDescriptionTask());
        } else {
            throw new IllegalArgumentException("Неизвестный тип задачи: " + task.getClass());
        }
    }

    public static FileBackedTaskManager loadFromFile(File file, HistoryManager historyManager) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.size() > 1) {
                for (String line : lines.subList(1, lines.size())) {
                    if (line.trim().isEmpty()) continue;
                    Task task = fromString(line);
                    if (task instanceof Subtask) {
                        manager.createSubtask((Subtask) task);
                    } else if (task instanceof Epic) {
                        manager.createEpic((Epic) task);
                    } else {
                        manager.createTask(task);
                    }
                    historyManager.add(task);
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении данных в файл", e);
        }
        return manager;
    }

    private static Task fromString(String value) {
        String[] fields = value.split(",");
        if (fields.length < 5) {
            throw new IllegalArgumentException("Недостаточно данных для создания задачи");
        }
        final int id = Integer.parseInt(fields[0]);
        final TaskType type = TaskType.valueOf(fields[1]);
        final String name = fields[2];
        final TaskStatus status = TaskStatus.valueOf(fields[3]);
        final String description = fields[4];
        final int epicId = fields.length > 5 ? Integer.parseInt(fields[5]) : 0;

        return switch (type) {
            case TASK -> new Task(id, name, description, status);
            case EPIC -> new Epic(id, name, description);
            case SUBTASK -> new Subtask(id, name, description, status, epicId);
        };
    }
}


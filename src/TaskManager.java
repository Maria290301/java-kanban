import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private static int countID = 0;
    HashMap<Integer, Task> taskList = new HashMap<>();
    HashMap<Integer, Subtask> subtaskList = new HashMap<>();
    HashMap<Integer, Epic> epicLists = new HashMap<>();

    public Task createTask(Task task) {
        countID++;
        task.setId(countID);
        taskList.put(task.getId(), task);
        return task;
    }

    public Subtask createSubtask(Subtask subtask) {
        countID++;
        subtask.setId(countID);
        subtaskList.put(countID, subtask);
        final Epic epic = epicLists.get(subtask.getEpicId());
        epic.subtasks.add(subtask);
        return subtask;
    }

    public Epic createEpic(Epic epic) {
        countID++;
        epic.setId(countID);
        epicLists.put(epic.id, epic);
        return epic;
    }

    public void printTasks() {
        if (taskList.isEmpty()) {
            System.out.println("Список пуст");
        } else {
            for (int id : taskList.keySet()
            ) {
                String name = taskList.get(id).nameTask;
                System.out.println(id + ". " + name);
            }
        }
    }

    public void printEpic() {
        if (epicLists.isEmpty()) {
            System.out.println("Список пуст");
        } else {
            System.out.println(epicLists);
        }
    }

    public void removeAllTasks() {
        taskList.clear();
    }

    public void removeAllEpics() {
        subtaskList.clear();
        epicLists.clear();
    }

    public Task getTaskById(int id) {
        Task task = taskList.get(id);
        return task;
    }

    public Subtask getSubtaskId(int id) {
        Subtask subtask = subtaskList.get(id);
        return subtask;
    }

    public Epic getEpicById(int id) {
        Epic epic = epicLists.get(id);
        return epic;
    }

    public void updateTask(int id, Task task) {
        taskList.put(id, task);
    }

    public void updateSubtask(int id, Subtask subtask) {
        Subtask subtask1 = subtaskList.get(id);
        int idEpic = subtask1.getEpicId();
        Epic epic = epicLists.get(idEpic);
        subtask.setEpicId(subtask1.getEpicId());
        subtask.setDiscriptionTask(subtask1.getDiscriptionTask());
        subtask.setStatus(subtask1.getStatus());
        subtask.setId(subtask1.getId());
        epic.subtasks.remove(subtask1);
        epic.subtasks.add(subtask);
        epicLists.put(idEpic, epic);
    }

    public void updateEpic(int id, Epic epic2) {
        Epic epic = epicLists.get(id);
        epic2.setSubtasks(epic.getSubtasks());
        epic2.setStatus(epic.getStatus());
        epic2.setDiscriptionTask(epic.getDiscriptionTask());
        epicLists.put(id, epic2);
    }

    public void removeTaskById(int id) {
        taskList.remove(id);
    }

    public void removeSubtaskById(int id) {
        Subtask subtask1 = subtaskList.get(id);
        int idEpic = subtask1.getEpicId();
        Epic epic = epicLists.get(idEpic);
        epic.subtasks.remove(subtask1);
        epic.subtasks.remove(subtask1);
    }

    public void removeEpicById(int id) {
        Epic epic = epicLists.get(id);
        epicLists.remove(id);
        for (Subtask subtask : epic.subtasks) {
            if (subtask.getEpicId() == id) {
                epic.getSubtasks().remove(epic);
            }
        }
    }

    public void getStatusTask(Task task) {
        System.out.println(task.getStatus());
    }

    public void changeStatusTask(Task task) {
        if (task.status == TaskStatus.IN_PROGRESS) {
            task.setStatus(TaskStatus.DONE);
        } else if (task.status == TaskStatus.NEW) {
            task.setStatus(TaskStatus.IN_PROGRESS);
        } else {
            task.setStatus(TaskStatus.DONE);
        }
    }

    public void changeStatusSubtask(Subtask subtask) {
        if (subtask.status == TaskStatus.IN_PROGRESS) {
            subtask.setStatus(TaskStatus.DONE);
        } else if (subtask.status == TaskStatus.NEW) {
            subtask.setStatus(TaskStatus.IN_PROGRESS);
        } else {
            subtask.setStatus(TaskStatus.DONE);
        }
        Epic epic = epicLists.get(subtask.getEpicId());
        changeStatusEpic(epic);
    }

    public void getStatusEpic(Epic epic) {
        System.out.println(epic.nameTask + " - " + epic.getStatus() + ": ");
        ArrayList<Subtask> subtasks = epic.getSubtasks();
        for (Subtask subtask : subtasks) {
            System.out.println(subtask + " - " + subtask.getStatus());
        }
    }

    private void changeStatusEpic(Epic epic) {
        int count = 0;
        for (Subtask subtask : epic.getSubtasks()) {
            if (epic.subtasks == null) {
                epic.setStatus(TaskStatus.NEW);
            } else if (subtask.getStatus() == TaskStatus.IN_PROGRESS) {
                epic.setStatus(TaskStatus.IN_PROGRESS);
            } else if (subtask.getStatus() == TaskStatus.DONE) {
                count++;
            }
            if (count == epic.subtasks.size()) {
                epic.setStatus(TaskStatus.DONE);
            }
        }
    }

    public HashMap<Integer, Epic> getEpicLists() {
        return epicLists;
    }
}
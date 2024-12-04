package model;

import static org.junit.jupiter.api.Assertions.*;

import com.yandex.tracker.model.Epic;
import com.yandex.tracker.model.Subtask;
import com.yandex.tracker.service.TaskStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

class EpicTest {

    @Test
    void testAddSubtask() {
        Epic epic = new Epic("Epic 1", "Description 1");
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", TaskStatus.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", TaskStatus.NEW, epic.getId());

        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);

        List<Subtask> subtasks = epic.getSubtasks();
        assertEquals(2, subtasks.size(), "Epic should have 2 subtasks added.");
        assertTrue(subtasks.contains(subtask1), "Epic should contain subtask 1.");
        assertTrue(subtasks.contains(subtask2), "Epic should contain subtask 2.");
    }

    // Тестирование удаления подзадачи из эпика
    @Test
    void testRemoveSubtaskId() {
        Epic epic = new Epic("Epic 1", "Description 1");
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", TaskStatus.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", TaskStatus.NEW, epic.getId());

        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);

        epic.removeSubtask(subtask1.getId());

        List<Subtask> subtasks = epic.getSubtasks();
        assertEquals(1, subtasks.size(), "Epic should have 1 subtask remaining.");
        assertFalse(subtasks.stream().anyMatch(subtask -> subtask.getId() == subtask1.getId()),
                "Epic should not contain subtask 1 after removal.");
        assertTrue(subtasks.stream().anyMatch(subtask -> subtask.getId() == subtask2.getId()),
                "Epic should still contain subtask 2.");
    }

    @Test
    void testCleanSubtasks() {
        Epic epic = new Epic("Epic 1", "Description 1");
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", TaskStatus.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", TaskStatus.NEW, epic.getId());

        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);

        epic.cleanSubtasks();

        List<Subtask> subtasks = epic.getSubtasks();
        assertTrue(subtasks.isEmpty(), "Epic should have no subtasks after cleaning.");
    }
}

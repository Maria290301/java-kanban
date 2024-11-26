package test.model;

import com.yandex.tracker.model.Subtask;
import com.yandex.tracker.service.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubtaskTest {

    @Test
    void testSubtaskEqualityById() {
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", TaskStatus.NEW, 1);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", TaskStatus.NEW, 1);

        subtask1.setId(1);
        subtask2.setId(1);

        assertEquals(subtask1, subtask2, "Подзадачи должны быть равны по id.");
    }
}

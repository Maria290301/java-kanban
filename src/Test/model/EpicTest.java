package Test.model;

import static org.junit.jupiter.api.Assertions.*;
import com.yandex.tracker.model.Epic;
import org.junit.jupiter.api.Test;

class EpicTest {

    @Test
    void testEpicEqualityById() {
        Epic epic1 = new Epic("Subtask 1", "Description 1");
        Epic epic2 = new Epic("Subtask 1", "Description 1");

        epic1.setId(1);
        epic2.setId(1);

        assertEquals(epic1, epic2, "Эпики должны быть равны по id.");
    }
}

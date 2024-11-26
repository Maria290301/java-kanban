package com.yandex.tracker.service;

import com.yandex.tracker.model.Task;
import java.util.List;

public interface HistoryManager {

   void add(Task task);
   List<Task> getHistory();
}

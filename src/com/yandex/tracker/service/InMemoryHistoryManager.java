package com.yandex.tracker.service;

import com.yandex.tracker.model.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private Node<Task> first;
    private Node<Task> last;
    private final Map<Integer, Node<Task>> history = new HashMap<>();

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        Node<Task> node = history.get(task.getId());
        if (node != null) {
            removeNode(node);
        }
        linkLast(task);
    }

    @Override
    public void remove(int id) {
        removeNode(history.get(id));
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private void linkLast(Task element) {
        final Node<Task> oldLast = last;
        final Node<Task> newNode = new Node<>(oldLast, element, null);
        last = newNode;
        history.put(element.getId(), newNode);
        if (oldLast == null) {
            first = newNode;
        } else {
            oldLast.next = newNode;
        }
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node<Task> currentNode = first;
        while (!(currentNode == null)) {
            tasks.add(currentNode.task);
            currentNode = currentNode.next;
        }
        return tasks;
    }

    private void removeNode(Node<Task> node) {
        if (!(node == null)) {
            final Node<Task> next = node.next;
            final Node<Task> prev = node.prev;
            history.remove(node.task.getId());
            if (first == node && last == node) {
                first = null;
                last = null;
            } else if (first == node && !(last == node)) {
                first = next;
                first.prev = null;
            } else if (!(first == node) && last == node) {
                last = prev;
                last.next = null;
            } else {
                prev.next = next;
                next.prev = prev;
            }
        }
    }

    public static class Node<Task> {
        public Task task;
        public Node<Task> prev;
        public Node<Task> next;

        public Node(Node<Task> prev, Task task, Node<Task> next) {
            this.task = task;
            this.next = next;
            this.prev = prev;
        }
    }
}
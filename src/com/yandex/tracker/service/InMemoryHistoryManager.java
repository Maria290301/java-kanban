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
        final int id = task.getId();
        removeNode(history.get(id));
        linkLast(task);
        history.put(id, last);
    }

    @Override
    public void remove(int id) {
        Node<Task> nodeToRemove = history.remove(id);
        if (nodeToRemove != null) {
            removeNode(nodeToRemove);
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private void linkLast(Task task) {
        Node<Task> newNode = new Node<>(task);
        final Node<Task> oldLast = last;
        newNode.prev = oldLast;
        last = newNode;

        if (oldLast == null) {
            first = newNode;
        } else {
            oldLast.next = newNode;
        }
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node<Task> currentNode = first;
        while (currentNode != null) {
            tasks.add(currentNode.task);
            currentNode = currentNode.next;
        }
        return tasks;
    }

    private void removeNode(Node<Task> node) {
        if (node != null) {
            final Node<Task> next = node.next;
            final Node<Task> prev = node.prev;

            if (first == node && last == node) {
                first = null;
                last = null;
            } else if (first == node) {
                first = next;
                if (first != null) {
                    first.prev = null;
                }
            } else if (last == node) {
                last = prev;
                if (last != null) {
                    last.next = null;
                }
            } else {
                if (prev != null) {
                    prev.next = next;
                }
                if (next != null) {
                    next.prev = prev;
                }
            }
        }
    }

    public static class Node<T> {
        public T task;
        public Node<T> prev;
        public Node<T> next;

        public Node(T task) {
            this.task = task;
            this.next = null;
            this.prev = null;
        }
    }
}
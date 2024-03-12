package compression.lz77;

import java.util.ArrayList;
import java.util.List;

//Just a simple linked list implementation: https://www.geeksforgeeks.org/implementing-a-linked-list-in-java-using-class/

public class LinkedList {
    private Node head;
    private Node tail;
    public int size = 0;

    public LinkedList(int... values) {
        for (int value : values) { add(value); }
    }

    public LinkedList insert(int value) {
        if (head == null) { return add(value); }
        Node node = new Node(value);
        node.setNext(head);
        node.next().setPrev(node);
        head = node;
        size++;
        return this;
    }

    public LinkedList insertAfterHead(int value) {
        if (head == null) { return add(value); }
        if (head.next() == null) { return add(value); }

        Node node = new Node(value, head.next(), head);
        node.setPrev(head.next());

        head.next().setPrev(node);
        head.setNext(node);
        node.setPrev(head);

        size++;
        return this;
    }

    public LinkedList add(int value) {
        Node node = new Node(value);

        if (head == null) {
            head = tail = node;
        } else {
            tail.setNext(node);
            node.setPrev(tail);
            tail = node;
        }

        size++;
        return this;
    }

    public LinkedList remove(Node node) {
        if (head == null) { return this; }

        if (head == node) {
            head = head.next();
            if (head == null) {
                tail = null;
            } else {
                head.setPrev(null);
            }
            size--;
            return this;
        }

        if (tail == node) {
            tail = tail.prev();
            tail.setNext(null);
            size--;
            return this;
        }

        Node prevNode = node.prev();
        Node nextNode = node.next();
        prevNode.setNext(nextNode);
        nextNode.setPrev(prevNode);

        size--;
        return this;
    }

    //This doesn't do checks, but if we are sure that
    //next->next node exists then we should not care about it
    public void removeNextUnsafe(Node node) {
        node.next().next().setPrev(node);
        node.setNext(node.next().next());
        size--;
    }

    public Node head() {
        return head;
    }

    public Node tail() {
        return tail;
    }

    public int size() {
        return size;
    }

    public List<Integer> getList() {
        return getList(size);
    }

    public List<Integer> getList(int amount) {
        List<Integer> list = new ArrayList<>();
        Node current = head;

        while ((current != null) && (amount > 0)) {
            list.add(current.value());
            current = current.next();
            amount--;
        }

        return list;
    }
}

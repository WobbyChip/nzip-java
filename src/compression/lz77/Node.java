package compression.lz77;

//Just a simple node implementation: https://www.geeksforgeeks.org/implementing-a-linked-list-in-java-using-class/

public class Node {
    private int value;
    private Node next;
    private Node prev;

    public Node(int value) {
        this.value = value;
    }

    public Node(int value, Node next) {
        this(value);
        this.next = next;
    }

    public Node(int value, Node next, Node prev) {
        this(value, next);
        this.prev = prev;
    }

    public int value() {
        return value;
    }

    public Node next() {
        return next;
    }

    public Node prev() {
        return prev;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public void setPrev(Node prev) {
        this.prev = prev;
    }

    public void setValue(int value) {
        this.value = value;
    }
}

package compression.lz77;

//Just a simple node implementation: https://www.geeksforgeeks.org/implementing-a-linked-list-in-java-using-class/

public class Node {
    private int length;
    private int index;
    private Node next;
    private Node prev;

    public Node(int index) {
        this.index = index;
    }

    public Node(int value, Node next) {
        this(value);
        this.next = next;
    }

    public Node(int value, Node next, Node prev) {
        this(value, next);
        this.prev = prev;
    }

    public int index() {
        return index;
    }
    public int length() {
        return length;
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

    public void setIndex(int index) {
        this.index = index;
    }
    public void setLength(int length) {
        this.length = length;
    }
}

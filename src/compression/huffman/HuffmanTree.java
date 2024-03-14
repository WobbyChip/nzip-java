package compression.huffman;

import java.math.BigInteger;
import java.util.*;

//https://youtu.be/zSsTG3Flo-I

public class HuffmanTree {
    private final HashMap<Byte, Integer> frequencies = new HashMap<>();
    private final HashMap<Byte, Node> lookupTable = new HashMap<>();
    private Node root = null;

    public HuffmanTree(byte[] data) {
        createFrequencyMap(data);
        buildHuffmanTree(frequencies);
        buildLookupTable(root, "");
    }

    public HuffmanTree(HashMap<Byte, Integer> frequencies) {
        buildHuffmanTree(frequencies);
        buildLookupTable(root, "");
    }

    public HashMap<Byte, Node> getLookupTable() {
        return lookupTable;
    }

    public HashMap<Byte, Integer> getFrequencies() {
        return frequencies;
    }

    public Node getRoot() {
        return root;
    }

    private void createFrequencyMap(byte[] data) {
        frequencies.clear();

        for (byte b : data) {
            frequencies.put(b, frequencies.getOrDefault(b, 0)+1);
        }
    }

    private void buildHuffmanTree(HashMap<Byte, Integer> frequencies) {
        PriorityQueue<Node> queue = new PriorityQueue<>();

        for (Map.Entry<Byte, Integer> entry : frequencies.entrySet()) {
            queue.add(new Node(entry.getKey(), entry.getValue()));
        }

        if (queue.size() == 1) {
            queue.add(new Node((byte) 0, 1));
        }

        while (queue.size() > 1) {
            Node left = queue.poll();;
            Node right = queue.poll();
            Node parent = new Node((byte) 0, (left.getFrequency() + right.getFrequency()), left, right);
            queue.add(parent);
        }

        this.root = queue.poll();
    }

    private void buildLookupTable(Node node, String binary) {
        if (node.isLeaf()) {
            lookupTable.put(node.character, node.setBinary(binary));
        } else {
            buildLookupTable(node.leftNode, binary + "0");
            buildLookupTable(node.rightNode, binary + "1");
        }
    }

    static class Node implements Comparable<Node> {
        private long binary;
        private int binaryLength;
        private final byte character;
        private final int frequency;
        private Node leftNode;
        private Node rightNode;

        public Node(byte character, int frequency) {
            this(character, frequency, null, null);
        }

        public Node(byte character, int frequency, Node leftNode, Node rightNode) {
            this.character = character;
            this.frequency = frequency;
            this.leftNode = leftNode;
            this.rightNode = rightNode;
        }

        public int getFrequency() {
            return frequency;
        }

        public byte getCharacter() {
            return character;
        }

        public Node getLeftNode() {
            return leftNode;
        }

        public Node getRightNode() {
            return rightNode;
        }

        public boolean isLeaf() {
            return (this.leftNode == null) && (this.rightNode == null);
        }

        public Node setBinary(String binary) {
            this.binary = new BigInteger(binary, 2).longValue();
            this.binaryLength = binary.length();
            return this;
        }

        public long getBinary() {
            return binary;
        }

        public int getBinaryLength() {
            return binaryLength;
        }

        @Override
        public int compareTo(Node node) {
            int result = Integer.compare(frequency, node.getFrequency());
            return (result != 0) ? result : Integer.compare(character, node.getCharacter());
        }
    }
}

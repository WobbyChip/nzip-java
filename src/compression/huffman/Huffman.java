package compression.huffman;

import java.util.*;

//https://youtu.be/zSsTG3Flo-I

public class Huffman {
    public void compress(byte[] data) {
        HashMap<Byte, Integer> frequencyMap = createFrequencyMap(data);
        Node root = buildHuffmanTree(frequencyMap);
        Map<Byte, String> lookupTable = buildLookupTable(root);
        System.out.println(data.length + " " + (generateEncodedData(data, lookupTable).length()/8 + 1));
    }

    public HashMap<Byte, Integer> createFrequencyMap(byte[] data) {
        HashMap<Byte, Integer> frequencies = new HashMap<>();

        for (byte b : data) {
            frequencies.put(b, frequencies.getOrDefault(b, 0)+1);
        }

        return frequencies;
    }

    public Node buildHuffmanTree(HashMap<Byte, Integer> frequencies) {
        PriorityQueue<Node> queue = new PriorityQueue<>();
        //List<Map.Entry<Byte, Integer>> collect = frequencies.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toList());

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

        return queue.poll();
    }

    public Map<Byte, String> buildLookupTable(Node root) {
        HashMap<Byte, String> lookupTable = new HashMap<>();
        buildLookupTableHelper(root, "", lookupTable);
        System.out.println(lookupTable);
        return lookupTable;
    }

    public void buildLookupTableHelper(Node node, String code, Map<Byte, String> lookupTable) {
        if (node.isLeaf()) {
            lookupTable.put(node.character, code);
        } else {
            buildLookupTableHelper(node.leftNode, code + "0", lookupTable);
            buildLookupTableHelper(node.rightNode, code + "1", lookupTable);
        }
    }

    public String generateEncodedData(byte[] data, Map<Byte, String> lookupTable) {
        StringBuilder builder = new StringBuilder();

        for (byte b : data) {
            builder.append(lookupTable.get(b));
        }

        return builder.toString();
    }

    static class Node implements Comparable<Node> {
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

        public byte getCharatcer() {
            return character;
        }

        public boolean isLeaf() {
            return (this.leftNode == null) && (this.rightNode == null);
        }

        @Override
        public int compareTo(Node node) {
            int result = Integer.compare(frequency, node.getFrequency());
            return (result != 0) ? result : Integer.compare(character, node.getCharatcer());
        }
    }
}

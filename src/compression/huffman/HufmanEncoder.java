package compression.huffman;

import compression.BitCarry;

import java.util.*;
import java.util.function.Consumer;

public class HufmanEncoder {
    public static int MAX_FREQUENCY_BITS_LENGTH = 6; //Frequency is integer, integer max binary length is 32, 32 max binary length is 6
    public static int MAX_POSITIVE_INTEGER_LENGTH = 31; //This is size of positive integer

    public static byte[] compress(byte[] data) {
        return compress(data, ignored -> {});
    }

    public static byte[] compress(byte[] data, Consumer<Float> callback) {
        if (data.length == 0) { return data; }
        return encodedTree(data, new HuffmanTree(data), callback);
    }

    public static byte[] decompress(byte[] data) {
        return decompress(data, ignored -> {});
    }

    public static byte[] decompress(byte[] data, Consumer<Float> callback) {
        if (data.length == 0) { return data; }
        return decodedTree(data, callback);
    }

    private static byte[] encodedTree(byte[] data, HuffmanTree huffman, Consumer<Float> callback) {
        BitCarry bitCarry = new BitCarry();
        bitCarry.pushBits(data.length, MAX_POSITIVE_INTEGER_LENGTH);
        encodeHeader(bitCarry, huffman);

        for (int i = 0; i < data.length; i++) {
            HuffmanTree.Node node = huffman.getLookupTable().get(data[i]);
            bitCarry.pushBits(node.getBinary(), node.getBinaryLength());
            if (callback != null) { callback.accept((float) i/data.length*100); }
        }

        return bitCarry.getBytes(true);
    }

    private static byte[] decodedTree(byte[] data, Consumer<Float> callback) {
        BitCarry bitCarry = new BitCarry(data);
        int size = (int) bitCarry.getBits(MAX_POSITIVE_INTEGER_LENGTH);
        ArrayList<Byte> buffer = new ArrayList<>();
        HuffmanTree huffman = decodeHeader(bitCarry);
        HuffmanTree.Node node = null;

        while (buffer.size() < size) {
            if (node == null) { node = huffman.getRoot(); }
            byte binary = (byte) bitCarry.getBits(1);
            node = (binary == 0) ? node.getLeftNode() : node.getRightNode();
            if (!node.isLeaf()) { continue; }
            buffer.add(node.getCharacter());
            if (callback != null) { callback.accept((float) buffer.size()/size*100); }
            node = null;
        }

        return BitCarry.copyBytes(buffer);
    }

    private static void encodeHeader(BitCarry bitCarry, HuffmanTree huffman) {
        int size = huffman.getFrequencies().size() - 1; //This cannot be more than 256, and since byte is [0, 255] and size cannot be 0, therefore -1
        int max_frequency = Collections.max(huffman.getFrequencies().values()); //Get max frequency, we will use it to know how many bits to use for it
        int max_frequency_bits = Integer.toBinaryString(max_frequency).length(); //Get length of max frequency
        bitCarry.pushBits(max_frequency_bits, MAX_FREQUENCY_BITS_LENGTH); //Save max frequency size
        bitCarry.pushBits(size, 8); //Save frequency element count

        //Save frequencies to bit carry
        for (Map.Entry<Byte, Integer> entry : huffman.getFrequencies().entrySet()) {
            bitCarry.pushByte(entry.getKey());
            bitCarry.pushBits(entry.getValue(), max_frequency_bits);
        }
    }

    private static HuffmanTree decodeHeader(BitCarry bitCarry) {
        int max_frequency_bits = (int) bitCarry.getBits(MAX_FREQUENCY_BITS_LENGTH);
        int size = (int) (bitCarry.getBits(8) + 1);
        HashMap<Byte, Integer> frequencies = new HashMap<>();

        for (int i = 0; i < size; i++) {
            byte character = bitCarry.getByte();
            int frequency = (int) bitCarry.getBits(max_frequency_bits);
            frequencies.put(character, frequency);
        }

        return new HuffmanTree(frequencies);
    }
}

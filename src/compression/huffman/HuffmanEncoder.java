package compression.huffman;

import compression.BitCarry;

import java.util.*;
import java.util.function.Consumer;

public class HuffmanEncoder {
    private static final int MAX_FREQUENCY_BITS_LENGTH = 6; //Frequency is integer, integer max binary length is 32, 32 max binary length is 6
    private static final int MAX_POSITIVE_INTEGER_LENGTH = 31; //This is max size of positive integer in bits

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
        bitCarry.pushBits(data.length, MAX_POSITIVE_INTEGER_LENGTH); //We need to know data size so that we don't read final bits, which are not used
        encodeHeader(bitCarry, huffman); //Encode header, frequency map

        //Encode data from lookup table
        for (int i = 0; i < data.length; i++) {
            HuffmanTree.Node node = huffman.getLookupTable().get(data[i]);
            bitCarry.pushBits(node.getBinary(), node.getBinaryLength());
            if (callback != null) { callback.accept((float) i/data.length*100); }
        }

        return bitCarry.getBytes(true);
    }

    private static byte[] decodedTree(byte[] data, Consumer<Float> callback) {
        BitCarry bitCarry = new BitCarry(data);
        int size = (int) bitCarry.getBits(MAX_POSITIVE_INTEGER_LENGTH); //Get size of decoded file
        ArrayList<Byte> buffer = new ArrayList<>();
        HuffmanTree huffman = decodeHeader(bitCarry);
        HuffmanTree.Node node = huffman.getRoot();

        //Get bit by bit and navigate trough nodes to get back data until we reach file size
        while (buffer.size() < size) {
            byte binary = (byte) bitCarry.getBits(1); //Yes, this is not very efficient way, but I kinda don't care
            node = (binary == 0) ? node.getLeftNode() : node.getRightNode(); //Depending on bit we go left or right
            if (!node.isLeaf()) { continue; } //If node is a leaf, then we reached the end and should add data from it to buffer
            buffer.add(node.getCharacter()); //Add node's character to buffer
            if (callback != null) { callback.accept((float) buffer.size()/size*100); } //Just a simple progress callback
            node = huffman.getRoot(); //After adding character we need to go back to root and start over
        }

        return BitCarry.copyBytes(buffer);
    }

    private static void encodeHeader(BitCarry bitCarry, HuffmanTree huffman) {
        int size = huffman.getFrequencies().size() - 1; //This cannot be more than 256, and since byte is [0, 255] and size cannot be 0, therefore -1
        int max_frequency = Collections.max(huffman.getFrequencies().values()); //Get max frequency, we will use it to know how many bits to use for it
        int max_frequency_bits = Integer.toBinaryString(max_frequency).length(); //Get length of max frequency
        bitCarry.pushBits(max_frequency_bits, MAX_FREQUENCY_BITS_LENGTH); //Save max frequency size in bits, this will help to reduce space
        bitCarry.pushBits(size, 8); //Save frequency element count, so we know how much to read when decoding

        //Save frequencies to bit carry
        for (Map.Entry<Byte, Integer> entry : huffman.getFrequencies().entrySet()) {
            bitCarry.pushByte(entry.getKey());
            bitCarry.pushBits(entry.getValue(), max_frequency_bits);
        }
    }

    private static HuffmanTree decodeHeader(BitCarry bitCarry) {
        int max_frequency_bits = (int) bitCarry.getBits(MAX_FREQUENCY_BITS_LENGTH); //Get info, how much space does frequency take
        int size = (int) (bitCarry.getBits(8) + 1); //Then get info, how many frequencies we have
        HashMap<Byte, Integer> frequencies = new HashMap<>(); //Make hash map to load frequencies

        //Load frequencies into hashmap
        for (int i = 0; i < size; i++) {
            byte character = bitCarry.getByte();
            int frequency = (int) bitCarry.getBits(max_frequency_bits);
            frequencies.put(character, frequency);
        }

        //Build huffman tree from frequencies
        return new HuffmanTree(frequencies);
    }
}

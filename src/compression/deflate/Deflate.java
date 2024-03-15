package compression.deflate;


import compression.huffman.HuffmanEncoder;
import compression.lz77.LZ77Encoder;

import java.util.function.Consumer;

public class Deflate {
    public static byte[] compress(byte[] data) {
        return compress(data, ignored -> {});
    }

    public static byte[] compress(byte[] data, Consumer<Float> callback) {
        data = LZ77Encoder.compress(data, progress -> callback.accept(progress/2f));
        data = HuffmanEncoder.compress(data, progress -> callback.accept(49.9f + progress/2f));
        callback.accept(100f);
        return data;
    }

    public static byte[] decompress(byte[] data) {
        return decompress(data, ignored -> {});
    }

    public static byte[] decompress(byte[] data, Consumer<Float> callback) {
        data = HuffmanEncoder.decompress(data, progress -> callback.accept(progress/2f));
        data = LZ77Encoder.decompress(data, progress -> callback.accept(49.9f + progress/2f));
        callback.accept(100f);
        return data;
    }
}

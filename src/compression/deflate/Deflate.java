package compression.deflate;


import compression.huffman.HufmanEncoder;
import compression.lz77.LZ77Encoder;

import java.util.function.Consumer;

public class Deflate {
    public static byte[] compress(byte[] data) {
        return compress(data, ignored -> {});
    }

    public static byte[] compress(byte[] data, Consumer<Float> callback) {
        data = LZ77Encoder.compress(data, progress -> callback.accept(progress/2f));
        return HufmanEncoder.compress(data, progress -> callback.accept(50f + progress/2f));
    }

    public static byte[] decompress(byte[] data) {
        return decompress(data, ignored -> {});
    }

    public static byte[] decompress(byte[] data, Consumer<Float> callback) {
        data = HufmanEncoder.decompress(data, progress -> callback.accept(progress/2f));
        return LZ77Encoder.decompress(data, progress -> callback.accept(50f + progress/2f));
    }
}

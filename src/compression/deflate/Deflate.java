package compression.deflate;


import compression.helper.ProgressCallback;
import compression.huffman.HufmanEncoder;
import compression.lz77.LZ77;

public class Deflate {
    public static byte[] compress(byte[] data) {
        return compress(data, ignored -> {});
    }

    public static byte[] compress(byte[] data, ProgressCallback callback) {
        data = LZ77.compress(data, progress -> callback.onProgress(progress/2f));
        return HufmanEncoder.compress(data, progress -> callback.onProgress(50f + progress/2f));
    }

    public static byte[] decompress(byte[] data) {
        return decompress(data, ignored -> {});
    }

    public static byte[] decompress(byte[] data, ProgressCallback callback) {
        data = HufmanEncoder.decompress(data, progress -> callback.onProgress(progress/2f));
        return LZ77.decompress(data, progress -> callback.onProgress(50f + progress/2f));
    }
}

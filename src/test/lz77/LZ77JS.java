package test.lz77;

import compression.ProgressCallback;

import java.util.Arrays;

public class LZ77JS {
    public static final char REFERENCE_PREFIX = '`';
    public static final double MAX_STRING_DISTANCE = Math.pow(2, 16)-1;
    public static final int MIN_STRING_LENGTH = 5;
    public static final double MAX_STRING_LENGTH = Math.pow(2, 8)-1 + MIN_STRING_LENGTH;
    public static final double MAX_WINDOW_LENGTH = MAX_STRING_DISTANCE + MIN_STRING_LENGTH;
    public static final int ULTRA_COMPRESSION_LEVEL = 1000;

    private static byte[] encodeInt(int value, int width) {
        if ((value >= 0) && (value < (Math.pow(2, width*8)))) {
            byte[] data = new byte[width];

            for (int i = 0; i < width; i++) {
                data[width-i-1] = (byte) (value >>> (i * 8));
            }

            return data;
        } else {
            throw new IllegalArgumentException("Int out of range: " + value + " (width = " + width + ")");
        }
    }

    private static int decodeInt(byte[] data, int width) {
        if (data.length < width) {
            throw new IllegalArgumentException("Invalid byte array length: " + data.length + " (width = " + width + ")");
        }

        int value = 0;

        for (int i = 0; i < width; i++) {
            value = (value << 8) | (data[i] & 0xFF);
        }

        return value;
    }

    private static byte[] encodeLength(int length) {
        return encodeInt(length - MIN_STRING_LENGTH, 1);
    }

    private static int decodeLength(byte[] data) {
        return decodeInt(data, 1) + MIN_STRING_LENGTH;
    }

    private static byte[] encodeDistance(int distance) {
        return encodeInt(distance, 2);
    }

    private static int decodeDistance(byte[] data) {
        return decodeInt(data, 2);
    }

    public static boolean isArrayEqual(byte[] data, int start1, int end1, int start2, int end2) {
        end1 = Math.min(end1, data.length);
        end2 = Math.min(end2, data.length);
        if ((end1 - start1) != (end2 - start2)) { return false; }

        for (int i = start1, j = start2; i < end1; i++, j++) {
            if (data[i] != data[j]) { return false; }
        }

        return true;
    }

    public static byte[] compress(byte[] data, ProgressCallback callback) {
        return compress(data, ULTRA_COMPRESSION_LEVEL, callback);
    }

    public static byte[] compress(byte[] data, double windowLength, ProgressCallback callback) {
        if (windowLength > MAX_WINDOW_LENGTH) { return Arrays.copyOf(data, data.length); }

        LZ77ByteArrayOutputStream compressed = new LZ77ByteArrayOutputStream();
        int lastPos = data.length - MIN_STRING_LENGTH;
        int pos = 0;

        while (pos < lastPos) {
            int searchStart = Math.max(pos - (int) windowLength, 0);
            int matchLength = MIN_STRING_LENGTH;
            boolean foundMatch = false;
            int bm_distance = (int) MAX_STRING_DISTANCE;
            int bm_length = 0;

            while ((searchStart + matchLength) < pos) {
                boolean isValidMatch = isArrayEqual(data, searchStart, searchStart+matchLength, pos, pos+matchLength) && (matchLength < MAX_STRING_LENGTH);

                if (isValidMatch) {
                    matchLength++;
                    foundMatch = true;
                } else {
                    int realMatchLength = matchLength - 1;

                    if (foundMatch && (realMatchLength > bm_length)) {
                        bm_distance = pos - searchStart - realMatchLength;
                        bm_length = realMatchLength;
                    }

                    matchLength = MIN_STRING_LENGTH;
                    searchStart++;
                    foundMatch = false;
                }
            }

            if (bm_length != 0) {
                compressed.write(REFERENCE_PREFIX);
                compressed.write(encodeDistance(bm_distance));
                compressed.write(encodeLength(bm_length));
                pos += bm_length;
            } else {
                if (data[pos] != REFERENCE_PREFIX) {
                    compressed.write(data[pos]);
                } else {
                    compressed.write(new byte[] { REFERENCE_PREFIX, REFERENCE_PREFIX });
                }

                pos++;
            }

            if (callback != null) { callback.onProgress((float) pos/lastPos*100); }
        }

        //Write remaining bytes
        for (int i = pos; i < data.length; i++) {
            compressed.write(data[i]);
            if (data[i] == REFERENCE_PREFIX) { compressed.write(REFERENCE_PREFIX); }
        }

        return compressed.toByteArray();
    }

    public static byte[] decompress(byte[] data, ProgressCallback callback) {
        LZ77ByteArrayOutputStream decompressed = new LZ77ByteArrayOutputStream();
        int pos = 0;

        while (pos < data.length) {
            char currentChar = (char) data[pos];

            if (currentChar != REFERENCE_PREFIX) {
                decompressed.write(currentChar);
                pos++;
            } else {
                char nextChar = (char) data[pos+1];

                if (nextChar != REFERENCE_PREFIX) {
                    int distance = decodeDistance(new byte[] { data[pos+1], data[pos+2] });
                    int length = decodeLength(new byte[] { data[pos+3] });
                    decompressed.write(Arrays.copyOfRange(decompressed.getBytes(), decompressed.size()-distance-length, decompressed.size()));
                    pos += MIN_STRING_LENGTH - 1;
                } else {
                    decompressed.write(REFERENCE_PREFIX);
                    pos += 2;
                }
            }

            if (callback != null) { callback.onProgress((float) pos/data.length*100); }
        }

        return decompressed.toByteArray();
    }
}
package test.lz77;

import compression.lz77.BitCarry;
import compression.ProgressCallback;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class LZ77Test {
    public static int LOOK_AHEAD_BUFFER_SIZE = (1 << 8) - 1; //Max 255 (1 Byte)
    public static int SEARCH_BUFFER_SIZE = (1 << 16); //Max 65536 (2 Bytes)
    public static final int MIN_STRING_LENGTH = 3; //Token size is 3 bytes, there is no point in encoding string with length of 3 or less

    public static boolean isArrayEqual(byte[] data, int start1, int end1, int start2, int end2) {
        end1 = Math.min(end1, data.length);
        end2 = Math.min(end2, data.length);
        if ((end1 - start1) != (end2 - start2)) { return false; }

        for (int i = start1, j = start2; i < end1; i++, j++) {
            if (data[i] != data[j]) { return false; }
        }

        return true;
    }

    public static int[] nextToken(byte[] buffer, int pos) {
        boolean foundMatch = false;
        int start = Math.max(pos - LOOK_AHEAD_BUFFER_SIZE, 0);
        int matchLength = MIN_STRING_LENGTH;
        int distance = 0;
        int length = 0;

        while ((start + matchLength) < pos) {
            boolean isValidMatch = isArrayEqual(buffer, start, start+matchLength, pos, pos+matchLength) && (matchLength < SEARCH_BUFFER_SIZE);

            if (isValidMatch) {
                matchLength++;
                foundMatch = true;
            } else {
                int realMatchLength = matchLength - 1;

                if (foundMatch && (realMatchLength > length)) {
                    distance = pos - start - realMatchLength;
                    length = realMatchLength;
                }

                matchLength = MIN_STRING_LENGTH;
                start++;
                foundMatch = false;
            }
        }

        return new int[] { length, distance };
    }

    public static byte[] compress(byte[] data, ProgressCallback callback) {
        BitCarry bitCarry = new BitCarry();
        int end = data.length - MIN_STRING_LENGTH;
        int pos = 0;

        while (pos < end) {
            int[] token = nextToken(data, pos);
            int length = token[0];
            int distance = token[1];

            if (length > MIN_STRING_LENGTH) {
                bitCarry.pushBytes(true, (length & 0xff), ((distance >> 8) & 0xff), (distance & 0xff));
                pos += length;
            } else {
                bitCarry.pushBytes(false, data[pos]);
                pos++;
            }

            if (callback != null) { callback.onProgress((float) pos/end*100); }
        }

        //Write remaining bytes
        for (int i = pos; i < data.length; i++) {
            bitCarry.pushBytes(false, data[i]);
        }

        return bitCarry.getBytes(true);
    }

    public static byte[] decompress(byte[] input) {
        AtomicInteger pos = new AtomicInteger();
        ArrayList<Byte> output = new ArrayList<>();

        new BitCarry(input).decodeBytes(3).forEachRemaining(bt -> {
            if (!bt.bRefBit()) {
                output.add(bt.data()[0]);
                pos.addAndGet(1);
                return;
            }

            int length = bt.data()[0];
            int distance = (bt.data()[1] & 0xff) << 8 | (bt.data()[2] & 0xff);

            for (int i = 0; i < length; i++) {
                output.add(output.get(pos.get() - distance - length + i));
            }

            pos.addAndGet(length);
        });

        return BitCarry.copyBytes(output);
    }
}

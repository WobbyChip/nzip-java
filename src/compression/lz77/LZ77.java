package compression.lz77;

import compression.BitCarry;
import compression.ProgressCallback;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class LZ77 {
    //This is because encoded reference uses 3 bytes + 1 bit while raw data uses 3 bytes and 3 bits
    //Yes, we can set it to 3, because 25 < 27, but then we sacrifice length, so in average file it will get worse
    public static int MIN_DATA_LENGTH = 4;

    //255 + 4 => which is 1 byte used in encoding, remember [0, 1, 2, 3] are never used in length
    //So in the end we will have range [4; 259] - 4 => [0, 255]
    public static int LOOK_AHEAD_BUFFER_SIZE = (1 << 8) - 1 + MIN_DATA_LENGTH; //256 - 1 + 4 = 259

    //Because of cycling data new possible range is [1, 65536] in case of 111111111 -> 1<8, 1> so we add +1
    public static int MIN_DATA_DISTANCE = 1;

    public static int SEARCH_BUFFER_SIZE = (1 << 16) + MIN_DATA_DISTANCE; //[0; 65535] which is 2 bytes used in encoding

    public byte[] compress(byte[] data) {
        return compress(data, null);
    }

    public byte[] compress(byte[] data, ProgressCallback callback) {
        if (data.length == 0) { return data; }
        SuffixArray suffixArray = new SuffixArray(data, LOOK_AHEAD_BUFFER_SIZE, SEARCH_BUFFER_SIZE, MIN_DATA_LENGTH);
        Codec bitCarry = new Codec(); //Used to easily add data with ref bit
        int position = 0;

        while (position < data.length - MIN_DATA_LENGTH) {
            int[] reference = suffixArray.nextLongestMatch(position);
            int length = reference[0]; //Length of repeating data

            if (length >= MIN_DATA_LENGTH) {
                //If length more than 3 then encode distance and length and push them to bit carry
                //-1 because (1 << 8): 256, 256 is out of range for byte [0; 255], and -MIN_DATA_LENGTH, because if (length > MIN_DATA_LENGTH)
                int distance = position - reference[1] - MIN_DATA_DISTANCE; //Offset, aka, how much to go back
                //System.out.println("{ " + length + " " + (position - reference[1]) + " " + distance + " }");
                bitCarry.pushBytes(true, ((length - MIN_DATA_LENGTH) & 0xff), ((distance >> 8) & 0xff), (distance & 0xff));
                position += length;
            } else {
                //If length less than tree, then push byte as normal
                bitCarry.pushBytes(false, data[position]);
                position++;
            }

            if (callback != null) { callback.onProgress((float) position/data.length*100); }
        }

        //Write remaining bytes
        for (int i = position; i < data.length; i++) {
            bitCarry.pushBytes(false, data[i]);
        }

        if (callback != null) { callback.onProgress(100); }
        return bitCarry.getBytes(true);
    }

    public byte[] decompress(byte[] data) {
        return decompress(data, null);
    }

    public byte[] decompress(byte[] data, ProgressCallback callback) {
        if (data.length == 0) { return data; }
        AtomicInteger position = new AtomicInteger();
        ArrayList<Byte> output = new ArrayList<>();

        new Codec(data).decodeBytes(3).forEachRemaining(e -> {
            if (!e.bRefBit()) {
                //This part was left from test, but I will not remove it, in case if I want to test again
                for (int i = 0; i < e.data().length; i++) { output.add(e.data()[i]); }
                position.addAndGet(e.data().length);
                return;
            }

            int length = (e.data()[0] & 0xff) + MIN_DATA_LENGTH;
            int distance = ((e.data()[1] & 0xff) << 8 | (e.data()[2] & 0xff)) + MIN_DATA_DISTANCE;

            for (int i = 0; i < length; i++) {
                output.add(output.get(position.get() - distance + i));
            }

            position.addAndGet(length);
            if (callback != null) { callback.onProgress((float) position.get()/data.length*100); }
        });

        return BitCarry.copyBytes(output);
    }
}

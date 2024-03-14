package compression.lz77;

import compression.BitCarry;

import java.util.Iterator;

public class Codec extends BitCarry {
    public Codec() { super(); }
    public Codec(byte[] data) { super(data); }
    public record RefData(boolean bRefBit, byte[] data) {}

    public void pushBytes(boolean bRefBit, int... data) {
        if (bRefBit) { pushBits(1, 1); }

        for (int value : data) {
            if (!bRefBit) { pushBits(0, 1); }
            pushByte((byte) value);
        }
    }

    private RefData getRefData(int dataSize) {
        boolean bRefBit = getBits(1) == 1;
        int k = bRefBit ? dataSize : 1;
        byte[] bitBuffer = new byte[k];

        for (int i = 0; i < k; i++) { bitBuffer[i] = getByte(); }
        return new RefData(bRefBit, bitBuffer);
    }

    public Iterator<RefData> decodeBytes(int dataSize) {
        this.clear();
        pos = -1;

        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return pos+1 < data.length;
            }

            @Override
            public RefData next() {
                return getRefData(dataSize);
            }
        };
    }
}

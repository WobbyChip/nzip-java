package compression.lz77;

import compression.helper.BitCarry;

import java.util.Iterator;

public class LZ77Encoder extends BitCarry {
    public LZ77Encoder() { super(); }
    public LZ77Encoder(byte[] data) { super(data); }
    public record RefData(boolean bRefBit, int position, byte[] data) {}

    public void pushBytes(boolean bRefBit, int... data) {
        if (bRefBit) { pushBits(1, 1); }

        for (int value : data) {
            if (!bRefBit) { pushBits(0, 1); }
            pushByte((byte) value);
        }
    }

    private RefData getRefData(int dataSize) {
        boolean bRefBit = getBits(1) == 1;
        return new RefData(bRefBit, pos, getBytes(bRefBit ? dataSize : 1));
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

package compression.lz77;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BitCarry {
    //This is used to determine if data is normal byte or encoded data by adding 1 bit in front of data. (1 bit + 8 bits)
    //If the bit is 0 then data is a normal byte, otherwise it is encoded data
    //We also can save space by not adding 1 bit to each byte of encoded data, but only to first, but this requires to know size of encoded data

    public record RefData(boolean bRefBit, byte[] data) {}

    private final ArrayList<Byte> buffer = new ArrayList<>(); //With list it will be faster to add data, but worse for memory usage
    private byte[] data = new byte[0];
    private byte carry = 0; //Carrying byte, for example, 0b1101000
    private int carry_k = 0; //How many bits we are carrying right now
    private byte de_carry = 0; //Carrying byte for decoding
    private int de_carry_k = 0; //How many bits we are carrying right now
    private int pos = -1; //Position used in decoding data

    public BitCarry() {}
    public BitCarry(byte[] data) { this.data = data; }

    public void pushBits(byte data, int size) {
        data = (byte) ((data & 0xff) << (8 - size)); //Convert data to comfortable format : 00000101 => 10100000

        while (size > 0) {
            if (carry_k == 8) { buffer.add(carry); carry_k = carry = 0; } //If carry is full, empty it
            int move_carry_k = Math.min(size, (8 - carry_k)); //How many we can move

            //Add data to carry depending on free space of carry
            //0b11111111 >> 1 => 0b11111111 (STUPID JAVA) => use (data & 0xff)
            carry |= (byte) ((data & 0xff) >> carry_k);

            carry_k += move_carry_k; //Increase size of carry
            size -= move_carry_k; //Decrease size of data
            data = (byte) ((data & 0xff) << move_carry_k); //Shift data by move_carry_k (1): 10100000 => 01000000
        }
    }

    public void pushByte(byte data) {
        pushBits(data, 8);
    }

    public void pushBytes(boolean bRefBit, byte... data) {
        if (bRefBit) { pushBits((byte) 1, 1); }

        for (byte value : data) {
            if (!bRefBit) { pushBits((byte) 0, 1); }
            pushByte(value);
        }
    }

    public void pushBytes(boolean bRefBit, int... data) {
        if (bRefBit) { pushBits((byte) 1, 1); }

        for (int value : data) {
            if (!bRefBit) { pushBits((byte) 0, 1); }
            pushByte((byte) value);
        }
    }

    private byte getBits(int size, boolean reset) {
        if (size > 8) { throw new RuntimeException("max size is 8 bits"); }
        if (reset) { carry_k = carry = 0; }

        if (de_carry_k == 0) {
            pos += 1;
            de_carry = data[pos];
            de_carry_k = 8;
        }

        int move_carry_k = Math.min((size - carry_k), de_carry_k); //How many we can move
        //System.out.println("{ move_carry_k: " + move_carry_k + " carry: " + formatByte(carry) + " de_carry: " + formatByte(de_carry) + " carry_k: " + carry_k + " }");

        carry |= (byte) ((de_carry & 0xff) >> carry_k); //Move bits from de_carry to carry
        de_carry = (byte) ((de_carry & 0xff) << move_carry_k); //Update left bits in de_carry, move them to right

        carry_k += move_carry_k; //Update how many we are carrying in carry
        de_carry_k -= move_carry_k; //Update how many we are carrying in de_carry

        //Maybe there was not enough bits in de_carry
        if ((carry_k != size)) { return getBits(size, false); }
        return (byte) ((carry & 0xff) >> (8 - size));
    }

    public byte getBits(int size) {
        return getBits(size, true);
    }

    public byte getByte() {
        return getBits(8);
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

    private void clear() {
        this.buffer.clear();
        carry_k = carry = 0;
        de_carry_k = de_carry = 0;
    }

    private void flushCarry() {
        if (carry_k == 0) { return; }
        buffer.add(carry);
        carry_k = carry = 0;
    }

    public byte[] getBytes(boolean flush) {
        if (flush) { this.flushCarry(); }
        return copyBytes(buffer);
    }

    public static String formatByte(byte value) {
        return String.format("%8s", Integer.toBinaryString(value & 0xFF)).replace(' ', '0');
    }

    public static byte[] copyBytes(List<Byte> list) {
        byte[] byteArray = new byte[list.size()];

        for (int i = 0; i < list.size(); i++) {
            byteArray[i] = list.get(i);
        }

        return byteArray;
    }

    public static List<Byte> copyBytes(byte[] array) {
        ArrayList<Byte> byteList = new ArrayList<>();
        for (byte b : array) { byteList.add(b); }
        return byteList;
    }
}
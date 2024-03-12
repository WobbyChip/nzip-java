package compression.lz77;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BitCarry {
    //This is used to determine if data is normal byte or encoded data by adding 1 bit in front of data. (1 bit + 8 bits)
    //If the bit is 0 then data is a normal byte, otherwise it is encoded data
    //We also can save space by not adding 1 bit to each byte of encoded data, but only to first, but this requires to know size of encoded data

    public record BitData(boolean bRefBit, byte[] data) {}

    private final ArrayList<Byte> buffer = new ArrayList<>(); //With list it will be faster to add data, but worse for memory usage
    private byte carry = 0; //Carrying byte, for example, 0b1101000
    private int carry_k = 0; //How many bits we are carrying right now
    private byte de_carry = 0; //Carrying byte for decoding
    private int de_carry_k = 0; //How many bits we are carrying right now
    private int pos = -1; //Position used in decoding data

    private void pushByte(byte data, boolean bRefBit, boolean addRefBit) {
        // ^ 0 0 0 0 0 0 0 0 0 <- byte
        // ref bit,               byte is 8 bits but there are 9, so we will need to carry last bit
        //If we know size of encoded data, we don't need to add ref bit at beginning of every byte
        //System.out.println("D: " + formatByte(data) +  " | carry_k = " + carry_k + " | addRefBit = " + addRefBit + " | bRefBit = " + bRefBit);

        //Add leading bit to carry 0 or 1 => 0b1000000
        //if (addRefBit) { System.out.println("L: " + formatByte(carry) + " | " + formatByte((byte) ((bRefBit ? 0b1 : 0b0) << (8 - carry_k)))); }
        if (addRefBit) { carry |= (byte) (bRefBit ? (0b10000000 >> carry_k) : 0b0); }

        //Increase carry amount, how much we are carrying
        if (addRefBit) { carry_k += 1; }

        //If carry is full, empty it
        if (carry_k == 8) { buffer.add(carry); carry_k = carry = 0; }

        //Add data to carry depending on free space of carry
        //0b11111111 >> 1 => 0b11111111 (STUPID JAVA) => use (data & 0xff)
        //System.out.println("C: " + formatByte(carry) + " | " + formatByte((byte) ((data & 0xff) >> carry_k)));
        carry |= (byte) ((data & 0xff) >> carry_k);

        //At this point carry is full, write it to buffer
        buffer.add(carry);

        //Put remaining data in carry
        carry = (byte) (data << (8 - carry_k));
        //System.out.println("R: " + formatByte(carry));
    }

    public void pushBytes(boolean bRefBit, byte... data) {
        boolean addRefBit = true;

        for (byte value : data) {
            this.pushByte(value, bRefBit, addRefBit);
            addRefBit = !bRefBit;
        }
    }

    public void pushBytes(boolean bRefBit, int... data) {
        boolean addRefBit = true;

        for (int value : data) {
            this.pushByte((byte) value, bRefBit, addRefBit);
            addRefBit = !bRefBit;
        }
    }

    private byte getByte(byte[] data, boolean remvoeRefBit, boolean returnRefBit) {
        //data = 11111111 11110111 10100001 10000000
        //carry = 0b00000000, de_carry = 0b11111111
        //carry = 0b11111111, de_carry = 0b11110111 data -> 10100001 10000000

        //Remove ref bit in the beginning of carry
        if (remvoeRefBit) {
            carry = (byte) (carry << 1);
            carry_k = Math.max(carry_k-1, 0);
        }

        //If carry is full, clear it, we already returned it in previous run
        if (carry_k == 8) { carry_k = carry = 0; }

        //If de_carry_k is empty we need to use next byte
        //Store byte for decoding in another carry
        if (de_carry_k == 0) {
            pos += 1;
            de_carry = data[pos];
            de_carry_k = 8;
        }

        int free_carry_k = 8 - carry_k; //How many free bits we have
        int move_carry_k = Math.min(free_carry_k, de_carry_k); //How many we can move

        //carry_k = 5, de_carry_k = 2, free_carry_k = 3, move_carry_k = 2
        //11111000 11000000 -> 00000110

        carry |= (byte) ((de_carry & 0xff) >> carry_k); //Move bits from de_carry to carry
        de_carry = (byte) ((de_carry & 0xff) << move_carry_k); //Update left bits in de_carry, move them to right

        carry_k += move_carry_k; //Update how many we are carrying in carry
        de_carry_k -= move_carry_k; //Update how many we are carrying in de_carry

        //Maybe there was not enough bits in de_carry
        if (carry_k != 8) { return getByte(data, false, returnRefBit); }
        return returnRefBit ? (byte) ((carry & 0xff) >> 7) : carry;
    }

    private BitData getBitData(byte[] data, int dataSize) {
        boolean bRefBit = getByte(data, false, true) == 1;
        int k = bRefBit ? dataSize : 1;
        byte[] bitBuffer = new byte[k];
        boolean remvoeRefBit = true;

        for (int i = 0; i < k; i++) {
            bitBuffer[i] = getByte(data, remvoeRefBit, false);
            remvoeRefBit = false;
        }

        return new BitData(bRefBit, bitBuffer);
    }

    public Iterator<BitData> decodeBytes(byte[] data, int dataSize) {
        this.clear();
        pos = -1;

        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return pos+1 < data.length;
            }

            @Override
            public BitData next() {
                return getBitData(data, dataSize);
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

    public static byte[] copyBytes(List<Byte> array) {
        byte[] byteArray = new byte[array.size()];

        for (int i = 0; i < array.size(); i++) {
            byteArray[i] = array.get(i);
        }

        return byteArray;
    }
}
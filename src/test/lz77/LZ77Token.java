package test.lz77;

import java.io.IOException;

public class LZ77Token {
    public static int TOKEN_SIZE = 9;
    public int position;
    public int length;
    public char character;

    public LZ77Token(int position, int length, char character) {
        this.position = position;
        this.length = length;
        this.character = character;
    }

    public int getPosition() {
        return position;
    }
    public int getLength() {
        return length;
    }
    public char getCharacter() { return character; }

    @Override
    public String toString() {
        return "(" + position + ", " + length + ", " + character + ")";
    }

    public void write(LZ77ByteArrayOutputStream stream) {
        stream.write((position >> 24) & 0xFF);
        stream.write((position >> 16) & 0xFF);
        stream.write((position >> 8) & 0xFF);
        stream.write(position & 0xFF);

        stream.write((length >> 24) & 0xFF);
        stream.write((length >> 16) & 0xFF);
        stream.write((length >> 8) & 0xFF);
        stream.write(length & 0xFF);

        stream.write(character & 0xFF);
    }

    public static LZ77Token decode(byte[] data, int offset) {
        int position = ((data[offset] & 0xFF) << 24) | ((data[offset + 1] & 0xFF) << 16) | ((data[offset + 2] & 0xFF) << 8) | (data[offset + 3] & 0xFF);
        int length = ((data[offset + 4] & 0xFF) << 24) | ((data[offset + 5] & 0xFF) << 16) | ((data[offset + 6] & 0xFF) << 8) | (data[offset + 7] & 0xFF);
        byte character = data[offset + 8];
        return new LZ77Token(position, length, (char) character);
    }
}

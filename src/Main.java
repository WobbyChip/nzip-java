import compression.lz77.BitCarry;
import compression.lz77.LZ77;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;


public class Main {
    public static void main(String[] args) throws IOException {
        testCompress();
        //testCarry();
    }

    public static void testCarry() {
        BitCarry bitCarry = new BitCarry();
        //bitCarry.pushBytes(false, (byte) 0b10000011, (byte) 0b10000011);
        //bitCarry.pushBytes(false, (byte) 0xFF, (byte) 0xFF, (byte) 0b01000011);
        for (int i = 0; i < 100; i++) {
            bitCarry.pushBytes(true, (byte) 0b10000001, (byte) 0b11101111, (byte) 0b01000011);
            bitCarry.pushBytes(false, (byte) 0b00001111);
            bitCarry.pushBytes(false, (byte) 0b00001111);
            bitCarry.pushBytes(false, (byte) 0b00001111);
            bitCarry.pushBytes(true, (byte) 0b00001111, (byte) 0b11111111, (byte) 0b01000011);
        }

        System.out.println();
        byte[] buffer = bitCarry.getBytes(true);

        for (byte b : buffer) {
            System.out.println(BitCarry.formatByte(b));
        }

        System.out.println();

        //BitCarry bitCarry1 = new BitCarry(buffer);
        //System.out.println("T: " + BitCarry.formatByte(bitCarry1.getBits(8)));
        //System.out.println("T: " + BitCarry.formatByte(bitCarry1.getBits(8)));

        new BitCarry(buffer).decodeBytes(3).forEachRemaining(e -> {
            for (byte b : e.data()) {
                System.out.print(BitCarry.formatByte(b) + " ");
            }

            System.out.println(e.bRefBit());
        });
    }

    public static void compress(String filename) throws IOException {
        byte[] rawData = Files.readAllBytes(Paths.get("files\\" + filename));

        //byte[] compressed = new LZ77().compress(rawData);
        byte[] compressed = new LZ77().compress(rawData, progress -> System.out.print((progress >= 100 ? "\n" : "\r") + "C: " + progress));
        byte[] decompressed = new LZ77().decompress(compressed);
        System.out.println("\n LZ77 nzip (" + filename + ") -> C: " + compressed.length + " | D: " + decompressed.length + " | R: " + rawData.length + " | Verify: " + Arrays.equals(rawData, decompressed));

        /*
        compressed = LZ77Test.compress(rawData, null);
        decompressed = LZ77Test.decompress(compressed);
        //Files.write(Paths.get("files\\monkey.decomp.bmp"), decompressed);
        System.out.println("\n LZ77 Test (Working) -> C: " + compressed.length + " | D: " + decompressed.length + " | Verify: " + Arrays.equals(rawData, decompressed));

        compressed = LZ77Compression.compress(rawData, null);
        decompressed = LZ77Decompression.decompress(compressed, null);
        System.out.println("\n LZ77 Python (Not working) -> C: " + compressed.length + " | D: " + decompressed.length + " | Verify: " + Arrays.equals(rawData, decompressed));

        compressed = LZ77JS.compress(rawData, null);
        decompressed = LZ77JS.decompress(compressed, null);
        System.out.println("\n LZ77 JS (Not working) -> C: " + compressed.length + " | D: " + decompressed.length + " | Verify: " + Arrays.equals(rawData, decompressed));
        compressed = Deflate.compress(rawData);
        decompressed = Deflate.decompress(compressed);
        System.out.println("\n Deflate -> C: " + compressed.length + " | D: " + decompressed.length + " | Verify: " + Arrays.equals(rawData, decompressed));
        */
    }

    public static void testCompress() throws IOException {
        compress("shrek.txt"); //(shrek.txt) -> C: 38170 | D: 70658 | R: 70658 | Verify: true
        compress("small.txt"); //(small.txt) -> C: 60 | D: 80 | R: 80 | Verify: true
        compress("empty.txt"); //(empty.txt) -> C: 0 | D: 0 | R: 0 | Verify: true
        compress("screenshot.png"); //(screenshot.png) -> C: 723081 | D: 645096 | R: 645096 | Verify: true
        compress("test.txt"); //(test.txt) -> C: 39945 | D: 184207 | R: 184207 | Verify: true
        compress("1234.txt"); //(1234.txt) -> C: 2424 | D: 200448 | R: 200448 | Verify: true
        compress("blank.bin"); //(blank.bin) -> C: 1239 | D: 102400 | R: 102400 | Verify: true
        compress("monkey.bmp"); //(monkey.bmp) -> C: 2930208 | D: 3686550 | R: 3686550 | Verify: true
    }
}

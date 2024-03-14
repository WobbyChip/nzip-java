import compression.deflate.Deflate;
import compression.BitCarry;
import compression.lz77.LZ77;
import compression.lz77.LZ77Encoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;


public class Main {
    public static void main(String[] args) throws IOException {
        testCompress();
        //testHuffman();
        //testCarry();
        //System.out.println(BitCarry.formatLong(Integer.MAX_VALUE));
        //System.out.println(BitCarry.formatLong(1));
    }

    public static void testCarry() {
        LZ77Encoder bitCarry = new LZ77Encoder();
        //bitCarry.pushBytes(false, (byte) 0b10000011, (byte) 0b10000011);
        //bitCarry.pushBytes(false, (byte) 0xFF, (byte) 0xFF, (byte) 0b01000011);
        for (int i = 0; i < 1; i++) {
            bitCarry.pushBytes(true, (byte) 0b10000001, (byte) 0b11101111, (byte) 0b01000011);
            bitCarry.pushBytes(false, (byte) 0b00001111);
            //bitCarry.pushBytes(false, (byte) 0b00001111);
            bitCarry.pushBytes(true, (byte) 0b00001111, (byte) 0b11111111, (byte) 0b01000011);
            bitCarry.pushBytes(true, (byte) 0b00001111, (byte) 0b11111111, (byte) 0b01000011);
        }

        System.out.println();
        byte[] buffer = bitCarry.getBytes(true);

        for (byte b : buffer) {
            System.out.println(BitCarry.formatByte(b));
        }

        System.out.println();

        System.out.println(BitCarry.formatLong(new LZ77Encoder(buffer).getBits(32)));

        //BitCarry bitCarry1 = new BitCarry(buffer);
        //System.out.println("T: " + BitCarry.formatByte(bitCarry1.getBits(8)));
        //System.out.println("T: " + BitCarry.formatByte(bitCarry1.getBits(8)));

        new LZ77Encoder(buffer).decodeBytes(3).forEachRemaining(e -> {
            for (byte b : e.data()) {
                System.out.print(BitCarry.formatByte(b) + " ");
            }

            System.out.println(e.bRefBit());
        });
    }

    public static void compress(String filename) throws IOException {
        byte[] rawData = Files.readAllBytes(Paths.get("files\\" + filename));

        byte[] comp = Deflate.compress(rawData, progress -> System.out.print("\rC: " + progress));
        byte[] decomp = Deflate.decompress(comp, progress -> System.out.print("\rD: " + progress));
        System.out.println("   nzip (" + filename + ") -> C: " + comp.length + " | D: " + decomp.length + " | R: " + rawData.length + " | Verify: " + Arrays.equals(rawData, decomp));
    }

    public static void testHuffman() throws IOException {
        byte[] rawData = Files.readAllBytes(Paths.get("files\\small.txt"));
        byte[] comp = LZ77.compress(rawData, progress -> System.out.print((progress == 100 ? "\n" : "\r") + "C: " + progress));
        byte[] decomp = LZ77.decompress(comp, progress -> System.out.print((progress == 100 ? "\n" : "\r") + "D: " + progress));

        System.out.println("\n" + comp.length + " " + rawData.length + " " + decomp.length + " | Verify: " + Arrays.equals(rawData, decomp));
        //Files.write(Paths.get("files\\test.txt.huff"), decomp);
        //System.out.println(new HuffmanEncoding().encode(rawData).length);
    }

    public static void testCompress() throws IOException {
        compress("shrek.txt"); //(shrek.txt) -> C: 38170 | D: 70658 | R: 70658 | Verify: true
        compress("small.txt"); //(small.txt) -> C: 60 | D: 80 | R: 80 | Verify: true
        compress("small_test.txt"); //(small_test.txt) -> C: 39 | D: 64 | R: 64 | Verify: true
        compress("screenshot.png"); //(screenshot.png) -> C: 723081 | D: 645096 | R: 645096 | Verify: true
        compress("test.txt"); //(test.txt) -> C: 39967 | D: 184207 | R: 184207 | Verify: true
        compress("1234.txt"); //(1234.txt) -> C: 2424 | D: 200448 | R: 200448 | Verify: true
        compress("blank.bin"); //(blank.bin) -> C: 1239 | D: 102400 | R: 102400 | Verify: true
        compress("monkey.bmp"); //(monkey.bmp) -> C: 2930208 | D: 3686550 | R: 3686550 | Verify: true
        compress("empty.txt"); //(empty.txt) -> C: 0 | D: 0 | R: 0 | Verify: true
    }
}

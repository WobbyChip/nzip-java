import compression.deflate.Deflate;
import compression.BitCarry;
import compression.huffman.HuffmanEncoder;
import compression.lz77.LZ77Encoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;


public class Test {
    public static void main(String[] args) throws IOException {
        testCompress();
        //testHuffman();
        //testCarry();
        //System.out.println(BitCarry.formatLong(Integer.MAX_VALUE));
        //System.out.println(BitCarry.formatLong(1));
    }

    public static void testCarry() {
        BitCarry bitCarry = new BitCarry();
        //bitCarry.pushBytes(false, (byte) 0b10000011, (byte) 0b10000011);
        //bitCarry.pushBytes(false, (byte) 0xFF, (byte) 0xFF, (byte) 0b01000011);
        for (int i = 0; i < 1; i++) {
            bitCarry.pushBytes((byte) 0b10000001, (byte) 0b11101111, (byte) 0b01000011);
            bitCarry.pushBytes((byte) 0b00001111);
            bitCarry.pushBytes((byte) 0b00001111);
            bitCarry.pushBytes((byte) 0b00001111, (byte) 0b11111111, (byte) 0b01000011);
            bitCarry.pushBytes((byte) 0b00001111, (byte) 0b11111111, (byte) 0b01000011);
        }

        System.out.println();
        byte[] buffer = bitCarry.getBytes(true);

        for (byte b : buffer) {
            System.out.println(BitCarry.formatByte(b));
        }

        System.out.println();

        System.out.println(BitCarry.formatLong(new BitCarry(buffer).getBits(32)));

        //BitCarry bitCarry1 = new BitCarry(buffer);
        //System.out.println("T: " + BitCarry.formatByte(bitCarry1.getBits(8)));
        //System.out.println("T: " + BitCarry.formatByte(bitCarry1.getBits(8)));
    }

    public static void compress(String filename) throws IOException {
        byte[] rawData = Files.readAllBytes(Paths.get("files\\" + filename));

        byte[] comp = Deflate.compress(rawData, progress -> System.out.print("\rC: " + progress));
        //Files.write(Paths.get("files\\" + filename + ".comp"), comp);
        byte[] decomp = Deflate.decompress(comp, progress -> System.out.print("\rD: " + progress));
        //Files.write(Paths.get("files\\" + filename + ".decomp"), decomp);
        System.out.println("   nzip (" + filename + ") -> C: " + comp.length + " | D: " + decomp.length + " | R: " + rawData.length + " | Ratio: " + ((float) rawData.length/comp.length) + " | Verify: " + Arrays.equals(rawData, decomp));
    }

    public static void testHuffman() throws IOException {
        byte[] rawData = Files.readAllBytes(Paths.get("files\\test.txt"));
        byte[] comp = HuffmanEncoder.compress(rawData); //, progress -> System.out.print((progress == 100 ? "\n" : "\r") + "C: " + progress));
        byte[] decomp = HuffmanEncoder.decompress(comp); //, progress -> System.out.print((progress == 100 ? "\n" : "\r") + "D: " + progress));

        System.out.println("\n" + comp.length + " " + rawData.length + " " + decomp.length + " | Verify: " + Arrays.equals(rawData, decomp));
        //Files.write(Paths.get("files\\test.txt.huff"), decomp);
        //System.out.println(new HuffmanEncoding().encode(rawData).length);
    }

    public static void testCompress() throws IOException {
        compress("File1.html"); //(File1.html) -> C: 26198 | D: 80479 | R: 80479 | Ratio: 3.071952 | Verify: true
        compress("File2.html"); //(File2.html) -> C: 81966 | D: 344523 | R: 344523 | Ratio: 4.203243 | Verify: true
        compress("File3.html"); //(File3.html) -> C: 23910 | D: 83069 | R: 83069 | Ratio: 3.4742367 | Verify: true
        compress("File4.html"); //(File4.html) -> C: 44754 | D: 206694 | R: 206694 | Ratio: 4.6184473 | Verify: true
        compress("test.txt"); //(test.txt) -> C: 38218 | D: 184207 | R: 184207 | Ratio: 4.8199015 | Verify: true
        compress("shrek.txt"); //(shrek.txt) -> C: 36606 | D: 70658 | R: 70658 | Ratio: 1.93023 | Verify: true
        compress("test_256.bin"); //(test_256.bin) -> C: 490 | D: 256 | R: 256 | Ratio: 0.52244896 | Verify: true
        compress("small.txt"); //(small.txt) -> C: 94 | D: 80 | R: 80 | Ratio: 0.85106385 | Verify: true
        compress("1byte.txt"); //(1byte.txt) -> C: 7 | D: 1 | R: 1 | Ratio: 0.14285715 | Verify: true
        compress("small_test.txt"); //(small_test.txt) -> C: 57 | D: 64 | R: 64 | Ratio: 1.122807 | Verify: true
        compress("screenshot.png"); //(screenshot.png) -> C: 682865 | D: 645096 | R: 645096 | Ratio: 0.9446904 | Verify: true
        compress("1234.txt"); //(1234.txt) -> C: 1065 | D: 200448 | R: 200448 | Ratio: 188.21408 | Verify: true
        compress("blank.bin"); //(blank.bin) -> C: 498 | D: 102400 | R: 102400 | Ratio: 205.6225 | Verify: true
        compress("monkey.bmp"); //(monkey.bmp) -> C: 2754556 | D: 3686550 | R: 3686550 | Ratio: 1.3383464 | Verify: true
        compress("empty.txt"); //(empty.txt) -> C: 0 | D: 0 | R: 0 | Ratio: NaN | Verify: true
    }
}

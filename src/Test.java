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
        compress("File1.html");
        compress("File2.html");
        compress("File3.html");
        compress("File4.html");
        compress("test.txt");
        compress("shrek.txt");
        compress("test_256.bin");
        compress("small.txt");
        compress("1byte.txt");
        compress("small_test.txt");
        compress("screenshot.png");
        compress("1234.txt");
        compress("blank.bin");
        compress("monkey.bmp");
        compress("empty.txt");

        /*
        (File1.html) -> C: 26506 | D: 80479 | R: 80479 | Ratio: 3.0362558 | Verify: true
        (File2.html) -> C: 82351 | D: 344523 | R: 344523 | Ratio: 4.1835923 | Verify: true
        (File3.html) -> C: 24163 | D: 83069 | R: 83069 | Ratio: 3.4378595 | Verify: true
        (File4.html) -> C: 45061 | D: 206694 | R: 206694 | Ratio: 4.5869823 | Verify: true
        (test.txt) -> C: 38569 | D: 184207 | R: 184207 | Ratio: 4.7760377 | Verify: true
        (shrek.txt) -> C: 36991 | D: 70658 | R: 70658 | Ratio: 1.9101403 | Verify: true
        (test_256.bin) -> C: 448 | D: 256 | R: 256 | Ratio: 0.5714286 | Verify: true
        (small.txt) -> C: 103 | D: 80 | R: 80 | Ratio: 0.776699 | Verify: true
        (1byte.txt) -> C: 9 | D: 1 | R: 1 | Ratio: 0.11111111 | Verify: true
        (small_test.txt) -> C: 71 | D: 64 | R: 64 | Ratio: 0.90140843 | Verify: true
        (screenshot.png) -> C: 717765 | D: 645096 | R: 645096 | Ratio: 0.89875656 | Verify: true
        (1234.txt) -> C: 1164 | D: 200448 | R: 200448 | Ratio: 172.20619 | Verify: true
        (blank.bin) -> C: 590 | D: 102400 | R: 102400 | Ratio: 173.55933 | Verify: true
        (monkey.bmp) -> C: 2841502 | D: 3686550 | R: 3686550 | Ratio: 1.2973949 | Verify: true
        (empty.txt) -> C: 0 | D: 0 | R: 0 | Ratio: NaN | Verify: true
         */
    }
}

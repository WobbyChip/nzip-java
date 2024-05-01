package fuck; //!!! REMOVE THIS LINE, BEFORE SENDING

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Main {
    public static String ABOUT = "000RDB000 Jānis Programmētājs";  //!!! UPDATE THIS LINE, BEFORE SENDING

    public static void main(String[] args) {
        Form.createWindow();
        Scanner sc = new Scanner(System.in);
        String sourceFile, resultFile, firstFile, secondFile;
        System.out.println("Enter command (comp, decomp, size, equal, about, gui, exit): ");

        while (true) {
            switch (sc.next()) {
                case "comp":
                    System.out.print("source file name: ");
                    sourceFile = sc.next();
                    System.out.print("archive name: ");
                    resultFile = sc.next();
                    comp(sourceFile, resultFile);
                    break;
                case "decomp":
                    System.out.print("archive name: ");
                    sourceFile = sc.next();
                    System.out.print("file name: ");
                    resultFile = sc.next();
                    decomp(sourceFile, resultFile);
                    break;
                case "size":
                    System.out.print("file name: ");
                    sourceFile = sc.next();
                    size(sourceFile);
                    break;
                case "equal":
                    System.out.print("first file name: ");
                    firstFile = sc.next();
                    System.out.print("second file name: ");
                    secondFile = sc.next();
                    System.out.println(equal(firstFile, secondFile));
                    break;
                case "about": about(); break;
                case "gui": Form.createWindow(); break;
                case "exit": System.exit(0); break;
            }
        }
    }

    public static void comp(String sourceFile, String resultFile) {
        try {
            String filename = Paths.get(sourceFile).getFileName().toString();
            byte[] data = Files.readAllBytes(Paths.get(sourceFile));
            data = Deflate.compress(data, progress -> System.out.printf(Locale.US, "\rCompressing %s: %.2f%%%s", filename, progress, (progress == 100 ? "\n" : "")));
            Files.write(Paths.get(resultFile), data);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void decomp(String sourceFile, String resultFile) {
        try {
            String filename = Paths.get(sourceFile).getFileName().toString();
            byte[] data = Files.readAllBytes(Paths.get(sourceFile));
            data = Deflate.decompress(data, progress -> System.out.printf(Locale.US, "\rDecompressing %s: %.2f%%%s", filename, progress, (progress == 100 ? "\n" : "")));
            Files.write(Paths.get(resultFile), data);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void size(String filename) {
        System.out.println("size: " + new File(filename).length());
    }

    public static boolean equal(String firstFile, String secondFile) {
        long f_size1 = new File(firstFile).length();
        long f_size2 = new File(secondFile).length();
        if (f_size1 != f_size2) { return false; }

        try {
            FileInputStream f1 = new FileInputStream(firstFile);
            FileInputStream f2 = new FileInputStream(secondFile);

            byte[] buffer1 = new byte[1024];
            byte[] buffer2 = new byte[1024];

            while ((f1.read(buffer1) != -1) && (f2.read(buffer2) != -1)) {
                if (!Arrays.equals(buffer1, buffer2)) { f1.close(); f2.close(); return false; }
            }

            f1.close();
            f2.close();
            return true;
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    public static void about() {
        System.out.println(ABOUT);
    }

    //////////////////////////////////////////////////////////////////////
    //Form ///////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    public static class Form {
        public static int WIDTH = 350;
        public static int HEIGHT = 200;
        public static int X_OFFSET = 10;
        public static int Y_OFFSET = 35;

        public static void createWindow() {
            JFrame jFrame = new JFrame("nzip Compressor");
            jFrame.setPreferredSize(new Dimension(WIDTH, HEIGHT));
            jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            jFrame.setLayout(null);
            jFrame.setResizable(false);

            JButton button0 = new JButton("i");
            button0.setMargin(new Insets(0, 0, 0, 0));
            button0.setBounds(5, 5, 15, 15);
            button0.setFocusPainted(false);
            jFrame.getContentPane().add(button0);

            JTextField textField1 = new JTextField("");
            textField1.setMargin(new Insets(0, 0, 0, 0));
            textField1.setBounds(X_OFFSET + 0, Y_OFFSET + 0, 210, 25);
            textField1.setEnabled(false);
            textField1.setDisabledTextColor(Color.GRAY);
            jFrame.getContentPane().add(textField1);

            JButton button1 = new JButton("Select File");
            button1.setBounds(X_OFFSET + 215, Y_OFFSET + 0, 100, 25);
            button1.setMargin(new Insets(0, 0, 0, 0));
            button1.setFocusPainted(false);
            jFrame.getContentPane().add(button1);

            JComboBox<CompressionType> comboBox1 = new JComboBox<>(CompressionType.COMPRESSION_TYPES);
            comboBox1.setBounds(X_OFFSET + 0, Y_OFFSET + 30, 210, 25);
            jFrame.getContentPane().add(comboBox1);

            JButton button2 = new JButton("Compress");
            button2.setMargin(new Insets(0, 0, 0, 0));
            button2.setBounds(X_OFFSET + 215, Y_OFFSET + 30, 100, 25);
            button2.setFocusPainted(false);
            jFrame.getContentPane().add(button2);

            JProgressBar progressBar1 = new JProgressBar(0, 100);
            progressBar1.setBounds(X_OFFSET + 0, Y_OFFSET + 60, 210, 25);
            progressBar1.setStringPainted(true);
            jFrame.getContentPane().add(progressBar1);

            JButton button3 = new JButton("Verify");
            button3.setMargin(new Insets(0, 0, 0, 0));
            button3.setBounds(X_OFFSET + 215, Y_OFFSET + 60, 100, 25);
            button3.setFocusPainted(false);
            jFrame.getContentPane().add(button3);

            JLabel label1 = new JLabel("");
            label1.setBounds(X_OFFSET + 0, Y_OFFSET + 90, 315, 25);
            label1.setText("Compression => ...");
            jFrame.getContentPane().add(label1);

            jFrame.pack();
            jFrame.setVisible(true);
            jFrame.setLocationRelativeTo(null);

            Consumer<Float> progressCallback = progress -> {
                progressBar1.setValue(Math.round(progress));
                progressBar1.setString(String.format(Locale.US, "%.2f%%", progress));
            };

            button0.addActionListener(e -> {
                JOptionPane.showMessageDialog(jFrame, Main.ABOUT, "About", JOptionPane.PLAIN_MESSAGE);
            });

            button1.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.home") + "/Desktop"));
                if (fileChooser.showOpenDialog(jFrame) != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                progressBar1.setValue(0);
                label1.setText("Compression => ...");
                textField1.setText(fileChooser.getSelectedFile().getAbsolutePath());
                CompressionType compressionType = CompressionType.getCompressed(textField1.getText());

                button2.setText((compressionType == null) ? "Compress" : "Decompress");
                comboBox1.setEnabled(compressionType == null);
                if (compressionType != null) {
                    comboBox1.setSelectedItem(compressionType);
                }
                jFrame.pack();
            });

            button2.addActionListener(e -> new Thread(() -> {
                if (textField1.getText().isEmpty()) {
                    return;
                }
                boolean compressing = comboBox1.isEnabled();
                CompressionType compressionType = comboBox1.getItemAt(comboBox1.getSelectedIndex());
                String comp_filename = textField1.getText() + compressionType.getExtension();
                String decomp_filename = textField1.getText().substring(0, Math.max(textField1.getText().lastIndexOf('.'), 0));
                String result_filename = compressing ? comp_filename : decomp_filename;

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File(result_filename));
                fileChooser.setSelectedFile(new File(result_filename));
                if (fileChooser.showSaveDialog(jFrame) != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                result_filename = fileChooser.getSelectedFile().getAbsolutePath();

                if (compressing && !result_filename.endsWith(compressionType.getExtension())) {
                    result_filename += compressionType.getExtension();
                }

                try {
                    button2.setEnabled(false);
                    button3.setEnabled(false);
                    byte[] data = Files.readAllBytes(Paths.get(textField1.getText()));
                    int filesize = data.length;
                    if (!compressing) {
                        data = compressionType.decompress(data, progressCallback);
                    }
                    if (compressing) {
                        data = compressionType.compress(data, progressCallback);
                    }
                    if (compressing) {
                        label1.setText(String.format(Locale.US, "Compression => Type: %s, Ratio: %.2f", compressionType.getName(), ((float) filesize / data.length)));
                    }
                    Files.write(Paths.get(result_filename), data);
                    JOptionPane.showMessageDialog(jFrame, "File was compressed/decompressed!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(jFrame, "Error reading/compressing file");
                    label1.setText("Error: " + ex.getMessage());
                }

                progressBar1.setValue(100);
                button2.setEnabled(true);
                button3.setEnabled(true);
            }).start());

            button3.addActionListener(e -> new Thread(() -> {
                if (textField1.getText().isEmpty()) {
                    button1.doClick();
                }
                if (textField1.getText().isEmpty()) {
                    return;
                }
                CompressionType compressionType = comboBox1.getItemAt(comboBox1.getSelectedIndex());

                try {
                    button2.setEnabled(false);
                    button3.setEnabled(false);
                    byte[] data = Files.readAllBytes(Paths.get(textField1.getText()));
                    byte[] comp_data = compressionType.compress(data, progress -> progressCallback.accept(progress / 2f));
                    byte[] decomp_data = compressionType.decompress(comp_data, progress -> progressCallback.accept(50f + progress / 2f));
                    label1.setText(String.format(Locale.US, "Verification => Type: %s, Ratio: %.2f, Verify: %b", compressionType.getName(), ((float) data.length / comp_data.length), Arrays.equals(data, decomp_data)));
                    JOptionPane.showMessageDialog(jFrame, "File was verified!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(jFrame, "Error reading/verifying file");
                    label1.setText("Error: " + ex.getMessage());
                }

                progressBar1.setValue(100);
                button2.setEnabled(true);
                button3.setEnabled(true);
            }).start());
        }
    }

    //////////////////////////////////////////////////////////////////////
    //CompressionType ////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    public enum CompressionType {
        DEFLATE("Deflate", ".nzip"),
        HUFFMAN("Huffman", ".huff"),
        LZSS("LZSS", ".lzss");

        public static final CompressionType[] COMPRESSION_TYPES = new CompressionType[] { DEFLATE, HUFFMAN, LZSS };
        private final String name;
        private final String extension;

        CompressionType(String name, String extension) {
            this.name = name;
            this.extension = extension;
        }

        // Getter methods
        public String getName() {
            return name;
        }

        public String getExtension() {
            return extension;
        }

        @Override
        public String toString() {
            return this.name;
        }

        public static CompressionType getCompressed(String filename) {
            String extension = filename.substring(Math.max(filename.lastIndexOf('.'), 0));
            return Stream.of(COMPRESSION_TYPES).filter(e -> e.getExtension().equalsIgnoreCase(extension)).findFirst().orElse(null);
        }

        @SafeVarargs
        public final byte[] compress(byte[] data, Consumer<Float>... callbacks) {
            return switch (this) {
                case DEFLATE -> Deflate.compress(data, callbacks);
                case HUFFMAN -> HuffmanEncoder.compress(data, callbacks);
                case LZSS -> LZ77Encoder.compress(data, callbacks);
            };
        }

        @SafeVarargs
        public final byte[] decompress(byte[] data, Consumer<Float>... callbacks) {
            return switch (this) {
                case DEFLATE -> Deflate.decompress(data, callbacks);
                case HUFFMAN -> HuffmanEncoder.decompress(data, callbacks);
                case LZSS -> LZ77Encoder.decompress(data, callbacks);
            };
        }
    }

    //////////////////////////////////////////////////////////////////////
    //BitCarry ///////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    public static class BitCarry {
        private static final int MAX_SIZE = 64;
        private final ArrayList<Byte> buffer = new ArrayList<>(); //With list, it will be faster to add data, but worse for memory usage
        private byte[] data = new byte[0]; //Data used when decoding
        private byte carry = 0; //Carrying byte, for example, 0b1101000
        private long carry_long = 0; //Carrying long, for example, 0b1101000(56)
        private int carry_k = 0; //How many bits we are carrying right now
        private byte de_carry = 0; //Carrying byte for decoding
        private int de_carry_k = 0; //How many bits we are carrying right now
        private int pos = -1; //Position used in decoding data

        public BitCarry() {}
        public BitCarry(byte[] data) { this.data = data; }

        public void pushBits(long data, int size) {
            if ((size < 1) || (size > MAX_SIZE)) { throw new RuntimeException(String.format("size must be in range [1; %d]", MAX_SIZE)); }
            data = data << (MAX_SIZE - size); //Convert data to comfortable format : (56)00000101 => 10100000(56)

            while (size > 0) {
                if (carry_k == 8) { buffer.add(carry); carry_k = carry = 0; } //If carry is full, empty it
                int move_carry_k = Math.min(size, (8 - carry_k)); //How many we can move

                //Add data to carry depending on free space of carry
                //0b11111111 >> 1 => 0b11111111 (STUPID JAVA) => use (data & 0xff)
                carry |= (byte) (data >>> (carry_k + (MAX_SIZE - 8))); //64 long - 8 byte => 56 bits + carry_k

                carry_k += move_carry_k; //Increase size of carry
                size -= move_carry_k; //Decrease size of data
                data = (data << move_carry_k); //Shift data by move_carry_k (1): 10100000(56) => 01000000(56)
            }
        }

        public void pushByte(byte data) {
            pushBits((data & 0xff), 8);
        }

        public void pushBytes(byte ...data) {
            for (byte b : data) { pushByte(b); }
        }

        public long getBits(int size, boolean reset, boolean shift) {
            if ((size < 1) || (size > MAX_SIZE)) { throw new RuntimeException(String.format("size must be in range [1; %d]", MAX_SIZE)); }
            if (reset) { carry_long = carry_k = 0; }

            if (de_carry_k == 0) {
                pos += 1;
                de_carry = data[pos];
                de_carry_k = 8;
            }

            int move_carry_k = Math.min((size - carry_k), de_carry_k); //How many we can move
            //System.out.println("{ move_carry_k: " + move_carry_k + " carry: " + formatByte(carry) + " de_carry: " + formatByte(de_carry) + " carry_k: " + carry_k + " }");

            long de_carry_long = (long) (de_carry & 0xff) << ((MAX_SIZE - 8) - carry_k); //Convert data to comfort format: 11000000 -> 11000000(56)
            carry_long |= (de_carry_long); //Move bits from de_carry to carry
            if (shift) { de_carry = (byte) ((de_carry & 0xff) << move_carry_k); } //Update left bits in de_carry, move them to right

            carry_k += move_carry_k; //Update how many we are carrying in carry
            if (shift) { de_carry_k -= move_carry_k; } //Update how many we are carrying in de_carry

            //Maybe there was not enough bits in de_carry
            if ((carry_k != size)) { return getBits(size, false, shift); }
            return (carry_long >>> (MAX_SIZE - size)); //So this is for what >>> is used
        }

        public long getBits(int size) {
            return getBits(size, true, true);
        }

        public byte getByte() {
            return (byte) getBits(8);
        }

        public byte[] getBytes(int size) {
            byte[] buffer = new byte[size];
            for (int i = 0; i < size; i++) { buffer[i] = getByte(); }
            return buffer;
        }

        public void clear() {
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

        public long getSize(boolean bits) {
            return buffer.size() * (bits ? 8L : 1L) + (bits ? carry_k : 0L);
        }

        public long availableSize(boolean bits) {
            return (data.length - (pos+1)) * (bits ? 8L : 1L) + (bits ? de_carry_k : 0L);
        }

        public static String formatLong(long value) {
            return String.format("%64s", Long.toBinaryString(value)).replace(' ', '0');
        }

        public static String formatByte(byte value) {
            return String.format("%8s", Integer.toBinaryString(value & 0xff)).replace(' ', '0');
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

    //////////////////////////////////////////////////////////////////////
    //SuffixArray ////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    public static class SuffixArray {
        private final HashMap<String, ArrayList<Integer>> suffixes = new HashMap<>(); //Place to store all combinations of suffixes and indexes
        private final byte[] buffer;
        private final int lookAheadBufferSize;
        private final int searchBufferSize;
        private final int suffixLength;
        private int lastPos;

        public SuffixArray(byte[] buffer, int lookAheadBufferSize, int searchBufferSize, int suffixLength) {
            this.buffer = buffer;
            this.lookAheadBufferSize = lookAheadBufferSize;
            this.searchBufferSize = searchBufferSize;
            this.suffixLength = suffixLength;
            this.lastPos = -suffixLength;
        }

        private String getSuffix(int position) {
            StringBuilder suffix = new StringBuilder(suffixLength);
            for (int i = 0; i < suffixLength; i++) { suffix.append((char) (buffer[position + i] & 0xff)); }
            return suffix.toString();
        }

        private void removeIndexesData(int position) {
            suffixes.remove(getSuffix(position));
        }

        private ArrayList<Integer> getIndexesData(int position, boolean create) {
            String suffix = getSuffix(position);

            if (!suffixes.containsKey(suffix) && create) {
                ArrayList<Integer> indexes = new ArrayList<>();
                suffixes.put(suffix, indexes);
                return indexes;
            }

            return suffixes.get(suffix);
        }

        //https://i.imgur.com/QQorJy6.png
        private void createSuffixes(int position) {
            int diff = position - lastPos;
            if (diff == 0) { return; }
            if (diff < 0) { throw new RuntimeException("going back not implemented"); }

            //Clear buffer in the past, this will have hit on performance, but least we will not run out of memory
            //+1 because (position - index >= searchBufferSize), >= is inclusive while "to" is exclusive se we add +1

            //(1 itr) to:    1000 - 100 => 900 + 1   position: 1000  | "to" is exclusive so 900 is cleared
            //(2 itr) from:  1000 - 100 => 900 + 1   lastPos: 1000   | "from" is inclusive, so we don't need to clear 900 again

            clearSuffixes((lastPos - searchBufferSize) + 1, (position - searchBufferSize) + 1);

            //position: 100 + SUFFIX_LENGTH => 103 => 0 to 103 -> [0, 102]
            //"from" is included, but "to" is not included: [from; to)
            createSuffixes((lastPos + suffixLength), (position + suffixLength));

            //Save current position as last for next time
            this.lastPos = position;
        }

        private void createSuffixes(int from, int to) {
            if (to + suffixLength - 1 > buffer.length) { to = buffer.length - suffixLength; }
            if (from < 0) { from = 0; }

            //System.out.println("[createSuffixes]: { " + from + " " + to + " }");

            //Initialize lists for all possible combinations to store their index
            for (int i = from; i < to; i++) { getIndexesData(i, true).add(i); }
        }

        //Same logic as createSuffixes(), but it just removes indexes and lists
        private void clearSuffixes(int from, int to) {
            if (to + suffixLength - 1 > buffer.length) { to = buffer.length - suffixLength; }
            if (from < 0) { from = 0; }

            //System.out.println("[clearSuffixes]: { " + from + " " + to + " }");

            for (int i = from; i < to; i++) {
                ArrayList<Integer> arrayList = getIndexesData(i, false);
                if (arrayList != null) { arrayList.remove((Object) i); } else { continue; }
                if (arrayList.isEmpty()) { removeIndexesData(i); }
            }
        }

        public int[] nextLongestMatch(int position) {
            //Load suffixes for next position we will be working with
            createSuffixes(position);

            //Get list of indexes of repeating data for current position
            ArrayList<Integer> indexes = getIndexesData(position, false);

            //Check if data (combination) for current position has indexes inside list
            int pos = Collections.binarySearch(indexes, position);

            //If it doesn't it means there is no repeating data, which means there is no even point going further
            if (pos-- == 0) { return new int[] { -1, -1 }; }

            //Make linked list to store matching indexes to later iterate over them
            LinkedList<Integer> indexesList = new LinkedList<>();

            //Collect indexes of repeating data which is inside our working buffer
            for (int i = pos; i >= 0; i--) {
                // [578 584 587 620 623 626 1797 2175 2178 2181] Indexes of data in file for specific suffix
                //         |     searchBufferSize    |           We can only go back as much as search buffer size allows us
                //                             <- position
                //linkedList: [587 620 623 626 1797 2175]        We insert after head, because we need to preserve same order, but loop goes backwards

                int index = indexes.get(i);
                if (position - index >= searchBufferSize) { break; } //Don't include indexes which go outisde search buffer size
                if (index < position) { indexesList.add(index); }; //Add those who do to linked list, because of my poor chunk implementation, there might be duplicates
            }

            //If no indexes found then just return
            if (indexesList.isEmpty()) { return new int[] { -1, -1 }; }

            //We don't need to check for previous bytes, because if suffix length would be 3,
            //That would mean that next 2 bytes from current position are the same
            int length = suffixLength;

            //Welp, I will try to explain this, but it is kinda hard to visualize:
            //We have "position": 2101 which in buffer corresponds to byte, for example, 0x4F
            //Now we have also indexes: "indexesList", which have indexes where same bytes appeared in last "searchBufferSize": 65536
            //Now we need to loop and increase length, and in each iteration compare if next byte from the indexes offset and current position are same: "match"
            //Also we need to check for boundaries, we cannot exceed buffer or go outside current position: "outside"
            //In the end we will be left with 1 index, that has the biggest length match

            //Implement cycling data: 12341234124 -> 1234<8, 4>, copy 1234 2 times in cycle
            //This means if index goes outside of current position go back at the index and
            //check if that works (DONE)

            loop: {
                while (length < lookAheadBufferSize) { //Loop 1 byte forward and compare if indexes do satisfy rules
                    if (position + length >= buffer.length) { break; } //We cannot go outside our buffer of data
                    Iterator<Integer> iterator = indexesList.iterator();

                    while (iterator.hasNext()) {
                        int index = iterator.next(); //This value is index of repeating data
                        boolean outside = (index + length >= position); //And we cannot (NOW WE CAN) go outside current position, because we search patterns in past to compress what is in front
                        int rlength = (outside ? (length % (position - index)) : length); //If we are outside position, we need to go back and check for repeating data
                        boolean match = (buffer[index + rlength] == buffer[position + length]); //Check if data: [index offset + length] = [position + length]

                        if (match) { continue; } //If this index satisfy rules, keep it
                        if (indexesList.size() == 1) { break loop; } //In this case we are left with final index, which is the biggest match
                        iterator.remove(); //Remove index, because data didn't match
                    }

                    length++;
                }
            }

            int offset = indexesList.getFirst(); //Get last remaining index
            return new int[] { (offset > -1 ? length : -1), offset };
        }
    }

    //////////////////////////////////////////////////////////////////////
    //LZ77Encoder ////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    public static class LZ77Encoder {
        @SafeVarargs
        public static byte[] compress(byte[] data, Consumer<Float>... callbacks) {
            return LZ77EncoderV2.compress(data, callbacks);
        }

        @SafeVarargs
        public static byte[] decompress(byte[] data, Consumer<Float>... callbacks) {
            return LZ77EncoderV2.decompress(data, callbacks);
        }
    }

    //////////////////////////////////////////////////////////////////////
    //LZ77EncoderV2 //////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    public class LZ77EncoderV2 {
        private static final int REFERENCE_LENGTH_SIZE = 8; //Size in bits to encode length
        private static final int REFERENCE_DISTANCE_SIZE = 16; //Size in bits to encode distance
        private static final int REFERENCE_SMALL_DISTANCE_SIZE = 10; //Size in bits to encode small distance

        //This is because encoded reference uses 3 bytes + 1 bit while raw data uses 3 bytes and 3 bits
        //Yes, we can set it to 3, because 25 < 27, but then we sacrifice length, so in average file it will get worse
        private static final int MIN_DATA_LENGTH = 4;

        //255 + 4 => which is 1 byte used in encoding, remember [0, 1, 2, 3] are never used in length
        //So in the end we will have range [4; 259] - 4 => [0, 255]
        private static final int LOOK_AHEAD_BUFFER_SIZE = (1 << REFERENCE_LENGTH_SIZE) - 1 + MIN_DATA_LENGTH; //256 - 1 + 4 = 259

        //Because of cycling data new possible range is [1, 65536] in case of 111111111 -> 1<8, 1> so we add +1
        private static final int MIN_DATA_DISTANCE = 1;
        private static final int SEARCH_BUFFER_SIZE = (1 << REFERENCE_DISTANCE_SIZE) + MIN_DATA_DISTANCE; //[0; 65535] which is 2 bytes used in encoding

        //Return generated huffman tree for frequencies of length of repeating data and also list of references
        @SafeVarargs
        private static Map.Entry<HuffmanTree, List<int[]>> generateHeader(BitCarry bitCarry, byte[] data, Consumer<Float>... callbacks) {
            SuffixArray suffixArray = new SuffixArray(data, LOOK_AHEAD_BUFFER_SIZE, SEARCH_BUFFER_SIZE, MIN_DATA_LENGTH);
            List<int[]> references = new ArrayList<>(); //Store position as of position in buffer and store length as of ref_length
            HashMap<Integer, Integer> frequencies = new HashMap<>(); //Store frequencies of repeating length values
            int position = 0;

            while (position < data.length - MIN_DATA_LENGTH) {
                int[] reference = suffixArray.nextLongestMatch(position);
                int length = reference[0]; //Length of repeating data

                if (length >= MIN_DATA_LENGTH) {
                    references.add(new int[] { position, length, reference[1] });
                    int ref_length = length - MIN_DATA_LENGTH;
                    frequencies.put(ref_length, frequencies.getOrDefault(ref_length, 0)+1);
                    position += length;
                } else {
                    position++;
                }

                for (Consumer<Float> callback : callbacks) { callback.accept((float) position/data.length*60f); }
            }

            HuffmanTree huffmanTree = new HuffmanTree(frequencies);
            HuffmanEncoder.encodeHeader(bitCarry, huffmanTree);
            return Map.entry(huffmanTree, references);
        }

        @SafeVarargs
        public static byte[] compress(byte[] data, Consumer<Float>... callbacks) {
            if (data.length == 0) { return data; }
            BitCarry bitCarry = new BitCarry(); //Used to easily manipulate bits
            bitCarry.pushBits(1, 1); // Determine if data is compressed or no

            Map.Entry<HuffmanTree, List<int[]>> header = generateHeader(bitCarry, data, callbacks);
            HuffmanTree huffmanTree = header.getKey();
            int position = 0; //Position for taking data from buffer

            //Write data with references
            for (int[] reference : header.getValue()) {
                while (position != reference[0]) { //Get end position where reference was taken from
                    for (Consumer<Float> callback : callbacks) { callback.accept(60+((float) position/data.length*30f)); }
                    boolean b1 = isLeadingOne(data[position], 8);
                    if (b1) { bitCarry.pushBits(1, 1); }  //This determines if next data is raw data that starts with 1 bit
                    bitCarry.pushBits(data[position], 8);
                    position++;
                }

                int length = reference[1]; //Length of repeating data
                int distance = reference[2]; //Position from where to copy data
                int offset = position - distance - MIN_DATA_DISTANCE; //Offset, aka, how much to go back
                int ref_length = length - MIN_DATA_LENGTH;
                boolean arg1 = (offset > ((1 << REFERENCE_SMALL_DISTANCE_SIZE) - 1));
                bitCarry.pushBits(0b10, 2); //This determines if next data encoded reference
                HuffmanTree.Node node = huffmanTree.getLookupTable().get(ref_length);
                bitCarry.pushBits(node.getBinary(), node.getBinaryLength()); //Save length as huffman binary path
                bitCarry.pushBits(arg1 ? 1 : 0, 1);
                bitCarry.pushBits(offset, arg1 ? REFERENCE_DISTANCE_SIZE : REFERENCE_SMALL_DISTANCE_SIZE);
                position += length;
            }

            boolean isBigger = (bitCarry.getSize(false) + (data.length - position)) > data.length;

            //In case if compressed data is bigger than original, there is no point in storing it
            if (isBigger) {
                bitCarry.clear();
                bitCarry.pushBits(0, 1);
                position = 0;
            }

            //Write remaining bytes as raw data
            for (int i = position; i < data.length; i++) {
                for (Consumer<Float> callback : callbacks) { callback.accept(90+((float) i/data.length*10f)); }
                if (!isBigger && isLeadingOne(data[i], 8)) { bitCarry.pushBits(1, 1); }
                bitCarry.pushBits(data[i], 8);
            }

            for (Consumer<Float> callback : callbacks) { callback.accept(100f); }
            return bitCarry.getBytes(true);
        }

        @SafeVarargs
        public static byte[] decompress(byte[] data, Consumer<Float>... callbacks) {
            if (data.length == 0) { return data; }
            BitCarry bitCarry = new BitCarry(data);
            boolean compressed = bitCarry.getBits(1) == 1;
            HuffmanTree huffmanTree = compressed ? HuffmanEncoder.decodeHeader(bitCarry) : null;
            ArrayList<Byte> output = new ArrayList<>();
            int position = 0;

            while (!compressed && (bitCarry.availableSize(false) > 0)) {
                output.add((byte) bitCarry.getBits(8, true, true));
                long done = data.length - bitCarry.availableSize(false); //Calculate how many bytes we processed
                for (Consumer<Float> callback : callbacks) { callback.accept((float) done/data.length*100); }
            }

            while (compressed && (bitCarry.availableSize(false) > 0)) {
                //D: 01110101 -> 01110101
                if (bitCarry.getBits(1, true, false) == 0) {
                    output.add((byte) bitCarry.getBits(8, true, true));
                    position += 1;
                    continue;
                }

                bitCarry.getBits(1, true, true); //It is definitely 1, we don't need it

                //D: 11110101 -> 1 11110101
                if ((bitCarry.getBits(1, true, false) == 1)) {
                    output.add((byte) bitCarry.getBits(8, true, true));
                    position += 1;
                    continue;
                }

                bitCarry.getBits(1, true, true); //It is definitely 0, we don't need it

                //R: 01110001 -> 1 0 01110001
                //R: 11110001 -> 1 0 11110001
                int length = getHuffmanLength(bitCarry, huffmanTree) + MIN_DATA_LENGTH; //Length is encoded as huffman binary path + MIN_DATA_LENGTH
                boolean arg1 = bitCarry.getBits(1) == 1; //Check if we have long or short distance
                int distance = (int) bitCarry.getBits(arg1 ? REFERENCE_DISTANCE_SIZE : REFERENCE_SMALL_DISTANCE_SIZE) + MIN_DATA_DISTANCE; //Distance is encoded as 2 byte and 1 byte is 16 bits

                //Copy bytes in loop from past
                for (int i = 0; i < length; i++) {
                    output.add(output.get((position - distance + i)));
                }

                position += length; //Increase position by reference length
                long done = data.length - bitCarry.availableSize(false); //Calculate how many bytes we processed
                for (Consumer<Float> callback : callbacks) { callback.accept((float) done/data.length*100); }
            }

            for (Consumer<Float> callback : callbacks) { callback.accept(100f); }
            return BitCarry.copyBytes(output);
        }

        public static boolean isLeadingOne(long data, int size) {
            return (((data >>> (size-1)) & 0x1) == 1);
        }

        private static int getHuffmanLength(BitCarry bitCarry, HuffmanTree huffmanTree) {
            HuffmanTree.Node node = huffmanTree.getRoot();

            //Get bit by bit and navigate trough nodes to get back data until we reach the end
            while (true) {
                byte binary = (byte) bitCarry.getBits(1); //Yes, this is not very efficient way, but I kinda don't care
                node = (binary == 0) ? node.getLeftNode() : node.getRightNode(); //Depending on bit we go left or right
                if (!node.isLeaf()) { continue; } //If node is a leaf, then we reached the end and should get data
                return node.getCharacter(); //Get length from huffman node
            }
        }
    }

    //////////////////////////////////////////////////////////////////////
    //HuffmanTree ////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    public static class HuffmanTree {
        private HashMap<Integer, Integer> frequencies = new HashMap<>();
        private final HashMap<Integer, Node> lookupTable = new HashMap<>();
        private Node root = null;

        public HuffmanTree(byte[] data) {
            createFrequencyMap(data);
            buildHuffmanTree(frequencies);
            buildLookupTable(root, "");
        }

        public HuffmanTree(HashMap<Integer, Integer> frequencies) {
            this.frequencies = frequencies;
            if (!frequencies.isEmpty()) { buildHuffmanTree(frequencies); }
            if (!frequencies.isEmpty()) { buildLookupTable(root, ""); }
        }

        public HashMap<Integer, Node> getLookupTable() {
            return lookupTable;
        }

        public HashMap<Integer, Integer> getFrequencies() {
            return frequencies;
        }

        public Node getRoot() {
            return root;
        }

        //Count frequencies of each byte, this will be used in tree building
        //Bytes with most frequencies, go up in tree, so that they have the smallest path
        //Each path is encoded as binary with 0 (left) and 1 (right)
        private void createFrequencyMap(byte[] data) {
            frequencies.clear();

            for (byte b : data) {
                frequencies.put(b & 0xff, frequencies.getOrDefault(b & 0xff, 0)+1);
            }
        }

        //Here we build the tree using frequencies
        //https://upload.wikimedia.org/wikipedia/commons/d/d8/HuffmanCodeAlg.png
        private void buildHuffmanTree(HashMap<Integer, Integer> frequencies) {
            PriorityQueue<Node> queue = new PriorityQueue<>();

            //Add all frequencies to queue
            for (Map.Entry<Integer, Integer> entry : frequencies.entrySet()) {
                queue.add(new Node(entry.getKey(), entry.getValue()));
            }

            //This is in case if data is 0 or 1 byte, we need at least 2 or 3 to create tree,
            //Then we just create empty node, it technically will never be used
            if (queue.size() == 1) {
                queue.add(new Node(0, 1));
            }

            //Now we need to combine all nodes together and build a tree, until we have left with 1 node which is root
            while (queue.size() > 1) {
                Node left = queue.poll();
                Node right = queue.poll(); //Merge 2 smallest by frequency nodes and create a new node
                Node parent = new Node(0, (left.getFrequency() + right.getFrequency()), left, right);
                queue.add(parent);
            }

            //Return final root of the tree
            this.root = queue.poll();
        }

        //Then after creating a tree we can create lookup table where we associate byte
        //with its new binary path, this is used to then encode those bytes
        private void buildLookupTable(Node node, String binary) {
            if (node.isLeaf()) {
                lookupTable.put(node.character, node.setBinary(binary));
            } else {
                buildLookupTable(node.leftNode, binary + "0");
                buildLookupTable(node.rightNode, binary + "1");
            }
        }

        public static class Node implements Comparable<Node> {
            private long binary;
            private int binaryLength;
            private final int character;
            private final int frequency;
            private final Node leftNode;
            private final Node rightNode;

            public Node(int character, int frequency) {
                this(character, frequency, null, null);
            }

            public Node(int character, int frequency, Node leftNode, Node rightNode) {
                this.character = character;
                this.frequency = frequency;
                this.leftNode = leftNode;
                this.rightNode = rightNode;
            }

            public int getFrequency() {
                return frequency;
            }

            public int getCharacter() {
                return character;
            }

            public Node getLeftNode() {
                return leftNode;
            }

            public Node getRightNode() {
                return rightNode;
            }

            public boolean isLeaf() {
                return (this.leftNode == null) && (this.rightNode == null);
            }

            public Node setBinary(String binary) {
                this.binary = new BigInteger(binary, 2).longValue();
                this.binaryLength = binary.length();
                return this;
            }

            public long getBinary() {
                return binary;
            }

            public int getBinaryLength() {
                return binaryLength;
            }

            @Override
            public int compareTo(Node node) {
                //This is the reason why queue returns smallest node
                int result = Integer.compare(frequency, node.getFrequency());
                return (result != 0) ? result : Integer.compare(character, node.getCharacter());
            }
        }
    }

    //////////////////////////////////////////////////////////////////////
    //HuffmanEncoder /////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    public static class HuffmanEncoder {
        private static final int MAX_FREQUENCY_BITS_LENGTH = 6; //Frequency is integer, integer max binary length is 32, 32 max binary length is 6
        private static final int MAX_POSITIVE_INTEGER_LENGTH = 31; //This is max size of positive integer in bits

        @SafeVarargs
        public static byte[] compress(byte[] data, Consumer<Float> ...callbacks) {
            if (data.length == 0) { return data; }
            return encodedTree(data, new HuffmanTree(data), callbacks);
        }

        @SafeVarargs
        public static byte[] decompress(byte[] data, Consumer<Float> ...callbacks) {
            if (data.length == 0) { return data; }
            return decodedTree(data, callbacks);
        }

        @SafeVarargs
        private static byte[] encodedTree(byte[] data, HuffmanTree huffmanTree, Consumer<Float> ...callbacks) {
            BitCarry bitCarry = new BitCarry();
            bitCarry.pushBits(1, 1); // Determine if data is compressed or no (BY DEFAULT YES)
            bitCarry.pushBits(data.length, MAX_POSITIVE_INTEGER_LENGTH); //We need to know data size so that we don't read final bits, which are not used
            encodeHeader(bitCarry, huffmanTree); //Encode header, frequency map

            //Encode data from lookup table
            for (int i = 0; i < data.length; i++) {
                HuffmanTree.Node node = huffmanTree.getLookupTable().get(data[i] & 0xff);
                bitCarry.pushBits(node.getBinary(), node.getBinaryLength());
                for (Consumer<Float> callback : callbacks) { callback.accept((float) i/data.length*100); }
            }

            //In case if compressed data is bigger than original, there is no point in storing it
            if (bitCarry.getSize(false) > data.length) {
                bitCarry.clear();
                bitCarry.pushBits(0, 1);
                bitCarry.pushBytes(data);
            }

            return bitCarry.getBytes(true);
        }

        @SafeVarargs
        private static byte[] decodedTree(byte[] data, Consumer<Float> ...callbacks) {
            BitCarry bitCarry = new BitCarry(data);
            boolean compressed = bitCarry.getBits(1) == 1;
            ArrayList<Byte> output = new ArrayList<>();

            if (compressed) {
                int size = (int) bitCarry.getBits(MAX_POSITIVE_INTEGER_LENGTH); //Get size of decoded file
                HuffmanTree huffman = decodeHeader(bitCarry);
                HuffmanTree.Node node = huffman.getRoot();

                //Get bit by bit and navigate trough nodes to get back data until we reach file size
                while (output.size() < size) {
                    byte binary = (byte) bitCarry.getBits(1); //Yes, this is not very efficient way, but I kinda don't care
                    node = (binary == 0) ? node.getLeftNode() : node.getRightNode(); //Depending on bit we go left or right
                    if (!node.isLeaf()) { continue; } //If node is a leaf, then we reached the end and should add data from it to buffer
                    output.add((byte) node.getCharacter()); //Add node's character to buffer
                    for (Consumer<Float> callback : callbacks) { callback.accept((float) output.size()/size*100); } //Just a simple progress callback
                    node = huffman.getRoot(); //After adding character we need to go back to root and start over
                }
            } else {
                while (bitCarry.availableSize(false) > 0) {
                    output.add((byte) bitCarry.getBits(8, true, true));
                    long done = data.length - bitCarry.availableSize(false); //Calculate how many bytes we processed
                    for (Consumer<Float> callback : callbacks) { callback.accept((float) done/data.length*100); }
                }
            }

            return BitCarry.copyBytes(output);
        }

        public static HuffmanTree encodeHeader(BitCarry bitCarry, HuffmanTree huffman) {
            boolean empty = huffman.getFrequencies().isEmpty();
            bitCarry.pushBits(empty ? 0b1 : 0b0, 1);
            if (empty) { return huffman; }

            int max_frequency = Collections.max(huffman.getFrequencies().values()); //Get max frequency, we will use it to know how many bits to use for it
            int max_frequency_bits = Integer.toBinaryString(max_frequency).length(); //Get length of max frequency
            int max_value = Collections.max(huffman.getFrequencies().keySet()); //Get max value, we will use it to know how many bits to use for it
            int max_value_bits = Integer.toBinaryString(max_value).length(); //Get length in bits of max value
            int size = huffman.getFrequencies().size() - 1; //This cannot be more than 256, and since byte is [0, 255] and size cannot be 0, therefore -1
            int size_bits = Integer.toBinaryString(size).length(); //Get size in bits for amount of elements count

            //System.out.println("[encodeHeader]: Size: " + size + ", Max Frequency Bits: " + max_frequency_bits + ", Max Value Bits: " + max_value_bits + ", Size Bits: " + size_bits);

            bitCarry.pushBits(max_frequency_bits, MAX_FREQUENCY_BITS_LENGTH); //Save max frequency size in bits, this will help to reduce space
            bitCarry.pushBits(max_value_bits, MAX_FREQUENCY_BITS_LENGTH); //Save max amount size in bits, this will help make it universal
            bitCarry.pushBits(size_bits, MAX_FREQUENCY_BITS_LENGTH); //Save max amount size in bits, this will help make it universal
            bitCarry.pushBits(size, size_bits); //Save frequency element count, so we know how much to read when decoding

            //Save frequencies to bit carry
            for (Map.Entry<Integer, Integer> entry : huffman.getFrequencies().entrySet()) {
                bitCarry.pushBits(entry.getKey(), max_value_bits);
                bitCarry.pushBits(entry.getValue(), max_frequency_bits);
            }

            return huffman;
        }

        public static HuffmanTree decodeHeader(BitCarry bitCarry) {
            boolean empty = bitCarry.getBits(1) == 1;
            if (empty) { return new HuffmanTree(new HashMap<>()); }

            int max_frequency_bits = (int) bitCarry.getBits(MAX_FREQUENCY_BITS_LENGTH); //Get info, how much space does frequency take
            int max_value_bits = (int) bitCarry.getBits(MAX_FREQUENCY_BITS_LENGTH); //Get info, how much space does size take
            int size_bits = (int) bitCarry.getBits(MAX_FREQUENCY_BITS_LENGTH); //Get info, how much space does size take
            int size = (int) (bitCarry.getBits(size_bits) + 1); //Then get info, how many frequencies we have
            HashMap<Integer, Integer> frequencies = new HashMap<>(); //Make hash map to load frequencies

            //System.out.println("[decodeHeader]: Size: " + size + ", Max Frequency Bits: " + max_frequency_bits + ", Max Value Bits: " + max_value_bits + ", Size Bits: " + size_bits);

            //Load frequencies into hashmap
            for (int i = 0; i < size; i++) {
                int character = (int) bitCarry.getBits(max_value_bits);
                int frequency = (int) bitCarry.getBits(max_frequency_bits);
                frequencies.put(character, frequency);
            }

            //Build huffman tree from frequencies
            return new HuffmanTree(frequencies);
        }
    }

    //////////////////////////////////////////////////////////////////////
    //Deflate ////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    public static class Deflate {
        @SuppressWarnings("unchecked")
        public static byte[] compress(byte[] data, Consumer<Float> ...callbacks) {
            data = LZ77Encoder.compress(data, progress -> Stream.of(callbacks).forEach(e -> e.accept(progress/2f)));
            data = HuffmanEncoder.compress(data, progress -> Stream.of(callbacks).forEach(e -> e.accept(49.9f + progress/2f)));
            for (Consumer<Float> callback : callbacks) { callback.accept(100f); }
            return data;
        }

        @SuppressWarnings("unchecked")
        public static byte[] decompress(byte[] data, Consumer<Float> ...callbacks) {
            data = HuffmanEncoder.decompress(data, progress -> Stream.of(callbacks).forEach(e -> e.accept(progress/2f)));
            data = LZ77Encoder.decompress(data, progress -> Stream.of(callbacks).forEach(e -> e.accept(49.9f + progress/2f)));
            for (Consumer<Float> callback : callbacks) { callback.accept(100f); }
            return data;
        }
    }
}
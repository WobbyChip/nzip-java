package compression.lz77;

import java.util.ArrayList;
import java.util.Collections;

//We create combination of all possible suffixes and they appearance places
//SUFFIX_LENGTH: 3 will create suffixes in combination of 3 bytes and record appearance of those combinations
//Then it will search for repeats of these combinations at these appearance places

//Btw this can be much better memory optimized if we would make it work with chunks of data instead
//of giving it entre buffer full of file data, but I DON'T CARE, because I only care about compression ratio,
//and memory usage or speed doesn't matter in this case. (KINDA BADLY DONE)

public class SuffixArray {
    public static final int SUFFIX_LENGTH = 1; //Don't put 2 or 3, 1 has same compression ratio and uses less memory
    private static final int ALPHABET_SIZE = 256; //There are 256 different bytes, therefore array size is 256
    private final MultiDimensionalArray<ArrayList<Integer>> suffixArray; //Multidimensional array to store all combinations and indexes
    private final byte[] buffer;
    private final int lookAheadBufferSize;
    private final int searchBufferSize;
    private int endOfSearchBuffer = 0;
    private int lastPos = 0;

    public SuffixArray(byte[] buffer, int lookAheadBufferSize, int searchBufferSize) {
        this.buffer = buffer;
        this.lookAheadBufferSize = lookAheadBufferSize;
        this.searchBufferSize = searchBufferSize;

        //There is not really point in using multidimensional array, it would be enough, to have just normal array of ArrayList<Integer>[ALPHABET_SIZE]
        //But this allows me to experiment, and I don't want to rewrite code, so I will leave this as it is
        this.suffixArray = new MultiDimensionalArray<>(SUFFIX_LENGTH, ALPHABET_SIZE);

        //Welp, we don't need to create suffix array for entire file in memory, but just a chunk of it
        //This will have impact on performance, but it is better than having out of memory issue
        this.createSuffixArray(0);
    }

    private void clearSuffixArray(int from, int to) {
        if (to > buffer.length) { to = buffer.length; }
        if (from < 0) { from = 0; }

        for (int i = from; i < to-SUFFIX_LENGTH+1; i++) {
            int[] indexes = new int[SUFFIX_LENGTH];
            for (int k = 0; k < SUFFIX_LENGTH; k++) { indexes[k] = (buffer[i+k] & 0xff); }
            ArrayList<Integer> arrayList = suffixArray.getValue(indexes);

            if (arrayList == null) { continue; }
            arrayList.remove((Object) i);
            if (arrayList.isEmpty()) { suffixArray.setValue(null, indexes); }
        }
    }

    private void createSuffixArray(int position) {
        int diff = position - lastPos;
        this.lastPos = position;

        //I really can't tell if this work properly or no, and are there any memory leaks
        //AND I DO NOT CARE, HEHE HAHA
        clearSuffixArray((position - searchBufferSize - diff - 1), (position - searchBufferSize));
        createSuffixArray((position - diff), (position + 1));
    }

    private void createSuffixArray(int from, int to) {
        if (to > buffer.length) { to = buffer.length; }
        if (from < 0) { from = 0; }
        this.endOfSearchBuffer = to;

        //Initialize lists for all possible combinations to store they appearances
        for (int i = from; i < to-SUFFIX_LENGTH+1; i++) {
            int[] indexes = new int[SUFFIX_LENGTH]; //Indexes are same as combinations

            //Then get combination from buffer, combinations are bytes, for example is suffix lenght is 3
            //First combination will be first 3 bytes of buffer, and it would cycle through entire buffer
            //Making these combinations and saving their positions
            for (int k = 0; k < SUFFIX_LENGTH; k++) { indexes[k] = (buffer[i+k] & 0xff); }

            ArrayList<Integer> arrayList = suffixArray.getValue(indexes); //Just get array list of positions for combination which is indexes
            if (arrayList == null) { arrayList = suffixArray.setValue(new ArrayList<>(), indexes); } //Create new array if it doesn't exist for this combination
            arrayList.add(i); //For combination add place of appearance
        }
    }

    public int[] nextReference(int position) {
        //Load suffix for next chunk we will be working with
        //This is bad, because we recreate suffix in the past, instead of just removing non-needed
        //BUT who am I to care, if it works, don't touch it
        if (position >= endOfSearchBuffer) { createSuffixArray(position); }

        //Get list of positions of repeating data for current position
        int[] combination = new int[SUFFIX_LENGTH];
        for (int k = 0; k < SUFFIX_LENGTH; k++) { combination[k] = buffer[position+k] & 0xff; }
        ArrayList<Integer> indexes = suffixArray.getValue(combination);

        //Check if data (combination) for current position has indexes inside list
        //If it doesn't it means there is no repeating data, which means there is no even point going further
        int pos = Collections.binarySearch(indexes, position);

        //If not found then just return
        if (pos-- == 0) { return new int[] { -1, -1 }; }

        //Make linked list with head: -1 and tail: -1, -1 means nothing found
        //This will make life easier since we will not need to check for nulls when returning value
        LinkedList linkedList = new LinkedList(-1, -1);

        //Collect indexes of repeating data inside our working buffer
        for (int i = pos; i >= 0; i--) {
            // [578 584 587 620 623 626 1797 2175 2178 2181] Indexes of data in file for specific suffix
            //         |     searchBufferSize    |           We can only go back as much as search buffer size allows us
            //                             <- position
            //linkedList: [-1 587 620 623 626 1797 2175 -1]  We insert after head, because we need to preserve same order, but loop goes backwards

            if (position - indexes.get(i) >= searchBufferSize) { break; } //Don't include indexes which go outisde search buffer size
            linkedList.insertAfterHead(indexes.get(i)); //Add those who do to linked list
        }

        //We don't need to check for previous bytes, because if SUFFIX_LENGTH would be 3,
        //that would mean that next 2 bytes from current position are the same
        int length = SUFFIX_LENGTH;
        Node node; //Used to iterate over nodes

        //Welp, I will try to explain this, but it is kinda hard to visualize:
        //We have "position": 2101 which in buffer corresponds to byte, for example, 0x4F
        //Now we have also nodes: "linkedList", which have indexes where same bytes appeared in last "searchBufferSize": 65536
        //Now we need to loop and increase length, and in each iteration compare if next byte from the node's offset and current position are same: arg2
        //Also we need to check for boundaries, we cannot exceed buffer or go outside current position: arg0, arg1
        //In the end we will be left with 1 node, that has the biggest length match

        loop: {
            while (length < lookAheadBufferSize) { //Loop 1 byte forward and compare if nodes do satisfy rules
                if (position + length >= buffer.length) { break; } //We cannot go outside our buffer of data
                node = linkedList.head(); //Another place without null checks, because head is -1
                int amount = linkedList.size() - 2; //-2 because we already have head and tail which are not really in here

                //Here we eliminate nodes which do not satisfy the rules
                for (int i = 0; i < amount; i++) {
                    boolean arg0 = (node.next().value() + length >= buffer.length); //And again, we cannot go outside our buffer of data
                    boolean arg1 = arg0 || (node.next().value() + length >= position); //And we cannot go outside current position, because we search patterns in past to compress what is in front
                    boolean arg2 = arg1 || (buffer[node.next().value() + length] != buffer[position + length]); //Check if data: [node offset + length] = [position + length]

                    if (!arg2) { node = node.next(); continue; } //If this node satisfy rules, keep it
                    if (linkedList.size() - 2 == 1) { break loop; } //In this case we are left with final node, which is the biggest match
                    linkedList.remove(node.next()); //Remove node, because data either didn't match or it went outside the boundaries
                }

                length++;
            }
        }

        //In case of no nodes left: head -> tails and value of tail is -1
        int distance = linkedList.head().next().value();
        return new int[] { (distance > 0 ? length : -1), distance };
    }
}

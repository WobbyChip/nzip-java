package compression.lz77;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class MultiDimensionalArray<T> {
    private Object arrays;
    private final int n_dimensions;
    private final int size;

    public MultiDimensionalArray(int n_dimensions, int size) {
        if (n_dimensions < 1) { throw new RuntimeException("wrong dimensions"); }
        if (size < 1) { throw new RuntimeException("wrong size"); }

        this.size = size;
        this.n_dimensions = n_dimensions;
        this.clear();
    }

    public T getValue(int ...indexes) {
        if (indexes.length < n_dimensions) { return null; }
        Object array = ((T[]) arrays)[indexes[0]];
        if (indexes.length == 1) { return (T) array; }

        for (int i = 1; i < indexes.length-1; i++) {
            array = ((T[]) array)[indexes[i]];
        }

        return ((T[]) array)[indexes[indexes.length-1]];
    }

    public T setValue(T value, int ...indexes) {
        if (indexes.length < n_dimensions) { return null; }

        if (indexes.length == 1) {
            return ((T[]) arrays)[indexes[0]] = value;
        }

        Object array = ((T[]) arrays)[indexes[0]];

        for (int i = 1; i < indexes.length-1; i++) {
            array = ((T[]) array)[indexes[i]];
        }

        ((T[]) array)[indexes[indexes.length-1]] = value;
        return value;
    }

    public int getSize() {
        return size;
    }

    public int getDimensions() {
        return n_dimensions;
    }

    public void clear() {
        int[] dimensions = new int[n_dimensions];
        Arrays.fill(dimensions, size);
        this.arrays = Array.newInstance(ArrayList.class, dimensions);
    }
}

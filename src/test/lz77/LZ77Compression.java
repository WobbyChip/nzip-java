package test.lz77;

//https://github.com/pynflate/pynflate/blob/master/src/pynflate/lz77.py
//https://github.com/pynflate/pynflate/blob/master/src/pynflate/lz77.py
//https://github.com/pynflate/pynflate/blob/master/src/pynflate/lz77.py

import compression.ProgressCallback;

public class LZ77Compression {
    private static final int SEARCH_AHEAD = 6; //Search ahead (NO TOUCHY)
    public static final int MAX_LONGEST = Integer.MAX_VALUE; // (int) (Math.pow(2, 16)-1); //Nope, 255 is bad idea, lets use 2 bytes, nope 2 bytes not enough as well

    private static LZ77Token nextToken(byte[] buffer, int position) {
        int longestMatchLen = 0;
        int longestMatchOffset = 0;
        int patternStartOffset = 1;
        int patternStartPos = position - patternStartOffset;

        while ((patternStartOffset < SEARCH_AHEAD) && (patternStartPos >= 0)) {
            int matchLen = match(buffer, patternStartPos, position);

            if (matchLen > longestMatchLen) {
                if (matchLen > MAX_LONGEST) { break; }
                longestMatchLen = matchLen;
                longestMatchOffset = patternStartOffset;
            }

            patternStartOffset++;
            patternStartPos--;
        }

        return new LZ77Token(longestMatchOffset, longestMatchLen, (char) buffer[position + longestMatchLen]);
    }

    private static int match(byte[] buffer, int patternPos, int matcheePos) {
        int matchLen = 0;

        while (matcheePos + matchLen + 1 < buffer.length) {
            if (matchLen > MAX_LONGEST) { break; }
            if (buffer[patternPos + matchLen] != buffer[matcheePos + matchLen]) { break; }
            matchLen++;
        }

        return matchLen;
    }

    public static byte[] compress(byte[] data, ProgressCallback callback) {
        LZ77ByteArrayOutputStream buffer = new LZ77ByteArrayOutputStream(0);
        int position = 0;

        while (position < data.length) {
            LZ77Token token = nextToken(data, position);
            position += token.getLength()+1;
            token.write(buffer);
            if (callback != null) { callback.onProgress((float) position/data.length*100); }
        }

        return buffer.toByteArray();
    }
}

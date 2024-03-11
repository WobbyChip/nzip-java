package test.lz77;

import compression.ProgressCallback;

public class LZ77Decompression {
    private static void decompressToken(StringBuilder buffer, LZ77Token token) {
        int position = buffer.length() - token.getPosition();

        for (int i = 0; i < token.getLength(); i++) {
            buffer.append(buffer.charAt(position));
            position++;
        }

        buffer.append(token.character);
    }

    public static byte[] decompress(byte[] data, ProgressCallback callback) {
        StringBuilder buffer = new StringBuilder();

        for (int idx = 0; idx < data.length; idx += LZ77Token.TOKEN_SIZE) {
            decompressToken(buffer, LZ77Token.decode(data, idx));
            if (callback != null) { callback.onProgress((float) idx/data.length*100); }
        }

        return buffer.toString().getBytes(); //Not sure about this tho
    }
}

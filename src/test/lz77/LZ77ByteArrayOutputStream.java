package test.lz77;

import java.io.ByteArrayOutputStream;

public class LZ77ByteArrayOutputStream extends ByteArrayOutputStream {
    private static final int TEMP_BUFFER_SIZE = 1024*1024;
    private ByteArrayOutputStream tempBuf;
    private boolean concatenating = false;

    public LZ77ByteArrayOutputStream() {
        this(0);
    }

    public LZ77ByteArrayOutputStream(int size) {
        super(size);
        this.tempBuf = new ByteArrayOutputStream(0);
    }

    @Override
    public synchronized int size() {
        return super.size() + tempBuf.size();
    }

    public void clear() {
        this.reset();
        this.buf = new byte[0];
        this.tempBuf = new ByteArrayOutputStream(0);
    }

    public byte[] getBytes() {
        this.concatenate();
        return this.buf;
    }

    @Override
    public synchronized byte[] toByteArray() {
        this.concatenate();
        return super.toByteArray();
    }

    private synchronized void concatenate() {
        this.concatenating = true;
        if (tempBuf.size() > 0) { this.write(tempBuf.toByteArray()); }
        this.tempBuf = new ByteArrayOutputStream(0);
        this.concatenating = false;
    }

    @Override
    public synchronized void write(int b) {
        if (this.concatenating) { super.write(b); return; }

        if (this.tempBuf.size()+1 >= TEMP_BUFFER_SIZE) {
            if (this.tempBuf.size() > 0) { this.concatenate(); }
            super.write(b);
        } else {
            this.tempBuf.write(b);
        }
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) {
        if (this.concatenating) { super.write(b, off, len); return; }

        if (this.tempBuf.size()+len >= TEMP_BUFFER_SIZE) {
            if (this.tempBuf.size() > 0) { this.concatenate(); }
            super.write(b, off, len);
        } else {
            this.tempBuf.write(b, off, len);
        }
    }

    @Override
    public void write(byte b[]) {
        write(b, 0, b.length);
    }
}

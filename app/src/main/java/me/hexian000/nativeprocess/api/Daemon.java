package me.hexian000.nativeprocess.api;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import static me.hexian000.nativeprocess.NativeProcess.TAG;

public class Daemon {
    private static final int BUFFER_SIZE = 4096;
    private final ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);
    private Process process = null;
    private InputStream in = null;

    public Daemon(final String shell, final String daemon) {
        try {
            process = new ProcessBuilder(shell).start();
            in = process.getInputStream();
            final OutputStream out = process.getOutputStream();
            out.write((daemon + "\n").getBytes());
            out.close();
        } catch (IOException ex) {
            Log.e(TAG, "Daemon", ex);
        }
    }

    public String safeReadLine() {
        if (process == null || in == null) {
            return null;
        }
        try {
            return readLine();
        } catch (IOException ignored) {
        }
        return null;
    }

    private int readLineInternal() throws IOException {
        while (buf.capacity() - buf.limit() > 0) {
            final int start = buf.limit();
            final int read = in.read(buf.array(), buf.limit(), buf.capacity() - buf.limit());
            buf.limit(buf.limit() + read);
            for (int i = start; i < buf.limit(); i++) {
                if (buf.get(i) == '\n') {
                    return i;
                }
            }
        }
        // cut it down
        return buf.limit() - 1;
    }

    private String readLine() throws IOException {
        for (int i = buf.position(); i < buf.limit(); i++) {
            if (buf.get(i) == '\n') {
                final String ret = new String(
                        buf.array(),
                        buf.position(),
                        i - buf.position());
                buf.position(i + 1);
                return ret;
            }
        }

        buf.compact();
        buf.flip();
        final int linePos = readLineInternal();
        final String ret = new String(buf.array(), buf.position(), linePos - buf.position());
        buf.position(linePos + 1);
        return ret;
    }

    public void close() {
        if (process != null) {
            process.destroy();
            process = null;
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException ignored) {
            }
            in = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}

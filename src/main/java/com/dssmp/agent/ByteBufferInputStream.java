package com.dssmp.agent;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.InvalidMarkException;

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ByteBufferInputStream extends InputStream {

    private static final int MAX_BYTE_UNSIGNED = 0xFF;
    private final ByteBuffer buffer;

    /**
     * Create a new InputStream that is linked to the provided ByteBuffer. The
     * buffer and this stream will share backing data, position, limit, and
     * mark, such that reading from this input stream will update the buffer
     * position; care should be taken to avoid concurrent reads and
     * modifications, as ByteBuffers are not thread-safe.
     */
    public ByteBufferInputStream(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public synchronized int read() {
        if (!buffer.hasRemaining()) {
            return -1;
        }
        return buffer.get() & MAX_BYTE_UNSIGNED;
    }

    @Override
    public synchronized int read(byte[] b) {
        return this.read(b, 0, b.length);
    }

    @Override
    public synchronized int read(byte[] b, int offset, int length) {
        if (!buffer.hasRemaining()) {
            return -1;
        }
        int ask = Math.min(buffer.remaining(), length);
        buffer.get(b, offset, ask);
        return ask;
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public synchronized void mark(int readlimit) {
        buffer.mark();
    }

    /**
     * Repositions this stream to the position at the time the mark method was
     * last called on this input stream.
     *
     * @throws IOException if this stream has not been marked
     */
    @Override
    public synchronized void reset() throws IOException {
        try {
            buffer.reset();
        } catch (InvalidMarkException e) {
            // Can't call rewind() because it might rewind us past the position
            // where this stream began
            throw new IOException("No mark set");
        }
    }

    @Override
    public synchronized int available() {
        return buffer.remaining();
    }

    @Override
    public synchronized long skip(long n) {
        if (n <= 0) {
            return 0;
        }
        // ask must be representable as an int, since buffer.remaining() can't
        // be more than Integer.MAX_VALUE
        int ask = (int) Math.min(buffer.remaining(), n);
        buffer.position(buffer.position() + ask);
        return ask;
    }

    /**
     * Closing a <code>ByteBufferInputStream</code> has no effect. The methods in
     * this class can be called after the stream has been closed without
     * generating an <code>IOException</code>.
     * <p>
     */
    @Override
    public void close() {
    }
}

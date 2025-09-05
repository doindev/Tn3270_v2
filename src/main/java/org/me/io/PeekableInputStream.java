package org.me.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class PeekableInputStream extends CircularBufferInputStream implements Closeable, AutoCloseable {

	public PeekableInputStream(InputStream in) {
		super(in);
	}

	public PeekableInputStream(InputStream in, int bufferSize) {
		super(in, bufferSize);
	}

	public boolean peek(byte[] sourceBuffer) throws IOException {
		if (sourceBuffer == null) {
			throw new NullPointerException("Source buffer cannot be null");
		}
		return peek(sourceBuffer, 0, sourceBuffer.length);
	}
	
	public boolean peek(byte[] sourceBuffer, int offset, int length) throws IOException {
		if (in == null) {
			throw new IOException("Stream is closed");
		}
		if (sourceBuffer == null) {
			throw new NullPointerException("Source buffer cannot be null");
		}
		if (offset < 0 || length < 0 || offset + length > sourceBuffer.length) {
			throw new IllegalArgumentException("Invalid offset or length");
		}
		
		if (haveBytes(length) < length) {
			return false;
		}
		
		return buffer.peek(sourceBuffer, offset, length);
	}

}

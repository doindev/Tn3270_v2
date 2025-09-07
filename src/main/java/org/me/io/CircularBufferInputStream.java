package org.me.io;

import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CircularBufferInputStream extends FilterInputStream implements Closeable, AutoCloseable{
	
	protected final CircularByteBuffer buffer;
	protected final int bufferSize;
	
	public CircularBufferInputStream(InputStream in) {
		this(in, CircularByteBuffer.DEFAULT_BUFFER_SIZE);
	}
	
	public CircularBufferInputStream(InputStream in, int bufferSize) {
		super(in);
		this.bufferSize = bufferSize;
		this.buffer = new CircularByteBuffer(bufferSize);
	}
	
	protected void fillBuffer() throws IOException {
		if (in == null) {
			throw new IOException("Stream is closed");
		}
		
		int space = buffer.getSpace();
		if (space > 0 && in.available() > 0) {
			int bytesToRead = Math.min(space, in.available());
			byte[] tempBuffer = new byte[bytesToRead];
			int bytesRead = in.read(tempBuffer, 0, bytesToRead);
			if (bytesRead > 0) {
				buffer.add(tempBuffer, 0, bytesRead);
			}
		}
	}
	
	protected int haveBytes(int count) throws IOException{
		if (buffer.getCurrentNumberOfBytes() >= count) {
			return buffer.getCurrentNumberOfBytes();
		}
		
		fillBuffer();
		return buffer.getCurrentNumberOfBytes();
	}
	
	@Override
	public int available() throws IOException {
		if (in == null) {
			throw new IOException("Stream is closed");
		}
		// Return bytes available in buffer plus bytes available in underlying stream
		return buffer.getCurrentNumberOfBytes() + in.available();
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		buffer.clear();
		in = null;
	}
	
	@Override
	public int read() throws IOException{
		if (in == null) {
			throw new IOException("Stream is closed");
		}
		
		if (!buffer.hasBytes()) {
			fillBuffer();
		}
		
		if (buffer.hasBytes()) {
			return buffer.read() & 0xFF;
		}
		
		// If no data available in buffer and underlying stream, return -1
		if (in.available() == 0) {
			return -1;
		}
		
		// Try one more time with blocking read for single byte
		int b = in.read();
		return b;
	}
	
	@Override
	public int read(byte[] targetBuffer, int offset, int length) throws IOException {
		if (in == null) {
			throw new IOException("Stream is closed");
		}
		if (targetBuffer == null) {
			throw new NullPointerException("Target buffer cannot be null");
		}
		if (offset < 0 || length < 0 || offset + length > targetBuffer.length) {
			throw new IndexOutOfBoundsException("Invalid offset or length");
		}
		if (length == 0) {
			return 0;
		}
		
		int totalBytesRead = 0;
		
		// First, try to read from buffer
		while (totalBytesRead < length && buffer.hasBytes()) {
			int bytesToRead = Math.min(length - totalBytesRead, buffer.getCurrentNumberOfBytes());
			buffer.read(targetBuffer, offset + totalBytesRead, bytesToRead);
			totalBytesRead += bytesToRead;
		}
		
		// If we still need more bytes and some were already read, return what we have
		if (totalBytesRead > 0) {
			return totalBytesRead;
		}
		
		// Try to fill buffer once more
		fillBuffer();
		
		// Read any available bytes from buffer
		if (buffer.hasBytes()) {
			int bytesToRead = Math.min(length, buffer.getCurrentNumberOfBytes());
			buffer.read(targetBuffer, offset, bytesToRead);
			return bytesToRead;
		}
		
		// If still no data in buffer, check if stream has data available
		if (in.available() == 0) {
			return -1;  // No data available
		}
		
		// Do a single blocking read as last resort
		return in.read(targetBuffer, offset, length);
	}
}

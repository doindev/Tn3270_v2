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
		if (space > 0) {
			byte[] tempBuffer = new byte[space];
			int bytesRead = in.read(tempBuffer, 0, space);
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
		
		return -1;
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
		
		while (totalBytesRead < length) {
			if (!buffer.hasBytes()) {
				fillBuffer();
			}
			
			if (!buffer.hasBytes()) {
				break;
			}
			
			int bytesToRead = Math.min(length - totalBytesRead, buffer.getCurrentNumberOfBytes());
			buffer.read(targetBuffer, offset + totalBytesRead, bytesToRead);
			totalBytesRead += bytesToRead;
		}
		
		return totalBytesRead == 0 ? -1 : totalBytesRead;
	}
}

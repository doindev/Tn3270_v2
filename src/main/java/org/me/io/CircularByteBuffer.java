package org.me.io;

public class CircularByteBuffer {
	public static final int DEFAULT_BUFFER_SIZE = 8192;
	
	private byte[] buffer;
	private int readIndex;
	private int writeIndex;
	private int currentNumberOfBytes;
	
	public CircularByteBuffer() {
		this(DEFAULT_BUFFER_SIZE);
	}
	
	public CircularByteBuffer(int bufferSize) {
		if (bufferSize <= 0) {
			throw new IllegalArgumentException("Buffer size must be positive");
		}
		this.buffer = new byte[bufferSize];
		this.readIndex = 0;
		this.writeIndex = 0;
		this.currentNumberOfBytes = 0;
	}

	
	public void add(byte b) throws IllegalStateException {
		if (!hasSpace()) {
			throw new IllegalStateException("Buffer is full");
		}
		buffer[writeIndex] = b;
		writeIndex = (writeIndex + 1) % buffer.length;
		currentNumberOfBytes++;
	}
	
	public void add(
		byte[] sourceBuffer, 
		int offset, 
		int length
	) throws IllegalStateException, IllegalArgumentException, NullPointerException {
		if (sourceBuffer == null) {
			throw new NullPointerException("Source buffer cannot be null");
		}
		if (offset < 0 || length < 0 || offset + length > sourceBuffer.length) {
			throw new IllegalArgumentException("Invalid offset or length");
		}
		if (!hasSpace(length)) {
			throw new IllegalStateException("Not enough space in buffer");
		}
		
		for (int i = 0; i < length; i++) {
			add(sourceBuffer[offset + i]);
		}
	}
	
	public void clear() {
		readIndex = 0;
		writeIndex = 0;
		currentNumberOfBytes = 0;
	}
	
	public int getCurrentNumberOfBytes() {
		return currentNumberOfBytes;
	}
	
	public int getSpace() {
		return buffer.length - currentNumberOfBytes;
	}
	
	public boolean hasBytes() {
		return currentNumberOfBytes > 0;
	}
	
	public boolean hasSpace() {
		return currentNumberOfBytes < buffer.length;
	}
	
	public boolean hasSpace(int count) {
		return getSpace() >= count;
	}
	
	public boolean peek(
		byte[] targetBuffer, 
		int offset, 
		int length
	) throws IllegalArgumentException, NullPointerException {
		if (targetBuffer == null) {
			throw new NullPointerException("Target buffer cannot be null");
		}
		if (offset < 0 || length < 0 || offset + length > targetBuffer.length) {
			throw new IllegalArgumentException("Invalid offset or length");
		}
		if (length > currentNumberOfBytes) {
			return false;
		}
		
		int tempReadIndex = readIndex;
		for (int i = 0; i < length; i++) {
			targetBuffer[offset + i] = buffer[tempReadIndex];
			tempReadIndex = (tempReadIndex + 1) % buffer.length;
		}
		return true;
	}
	
	public byte read() throws IllegalStateException {
		if (!hasBytes()) {
			throw new IllegalStateException("Buffer is empty");
		}
		byte result = buffer[readIndex];
		readIndex = (readIndex + 1) % buffer.length;
		currentNumberOfBytes--;
		return result;
	}
	
	public void read(
		byte[] targetBuffer, 
		int offset, 
		int length
	) throws IllegalStateException, IllegalArgumentException, NullPointerException {
		if (targetBuffer == null) {
			throw new NullPointerException("Target buffer cannot be null");
		}
		if (offset < 0 || length < 0 || offset + length > targetBuffer.length) {
			throw new IllegalArgumentException("Invalid offset or length");
		}
		if (length > currentNumberOfBytes) {
			throw new IllegalStateException("Not enough bytes in buffer");
		}
		
		for (int i = 0; i < length; i++) {
			targetBuffer[offset + i] = read();
		}
	}
}

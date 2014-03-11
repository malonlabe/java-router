package code.messy.net.ip.tcp;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

/*
 * Based on RingBuffer
 * TODO need to read from it without removing from queue
 * TODO may need to keep track of seq
 */
public class RetxQueue {
	private long seq;
	
	
	
	
	private int max = 8192;
	private byte[] buffer;
	private int start = 0;
	private int end = 0;
	private int size = 0;
	
	public RetxQueue() {
		buffer = new byte[max];
	}
	
	public RetxQueue(int max) {
		this.max = max;
		buffer = new byte[max];
	}
	
	public int size() {
		return size;
	}
	
	public int remaining() {
		return max - size;
	}

	public void write(ByteBuffer bb, int offset, int length) {
		// TODO fix efficiency
		byte[] b = new byte[length];
		int pos = bb.position();
		bb.position(offset);
		bb.get(b);
		bb.position(pos);
		write(b);
	}
	
	public void write(byte[] b) throws BufferOverflowException {
		write(b, 0, b.length);
	}
	
	public void write(byte[] b, int offset, int length) throws BufferOverflowException {
		int remain = remaining();
		
		if (length == 0) return;
		if (remain < length) throw new BufferOverflowException();

		if ((max - end) >= length) {
			// enough space at the end
			System.arraycopy(b, offset, buffer, end, length);
			end += length;
			if (end == max) end = 0;
		}
		else {
			int len1 = max - end;
			int len2 = length - len1;
			System.arraycopy(b, offset, buffer, end, len1);
			System.arraycopy(b, offset + len1, buffer, 0, len2);
			end = len2;
		}
		size += length;
	}

	public int read(byte[] b) {
		return read(b, 0, b.length);
	}
	
	public int read(byte[] b, int offset, int length) {
		int rlen, len1, len2;
		if (start < end) {
			len1 = end - start;
			len2 = 0;
		}
		else {
			len1 = max - start;
			len2 = end;
		}
		
		int remain = length;
		if (remain <= len1) {
			rlen = remain;
			System.arraycopy(buffer, start, b, offset, remain);
			start += rlen;
		}
		else {
			System.arraycopy(buffer, start, b, offset, len1);
			remain -= len1;
			if (remain < len2) len2 = remain;
			System.arraycopy(buffer, 0, b, offset + len1, len2);
			start = len2;
			rlen = len1 + len2;
		}
		size -= rlen;
		return rlen;
	}
	
	public int read(ByteBuffer bb) {
		// TODO fix efficiency
		byte[] b = new byte[1024];
		int length = read(b);
		bb.put(b, 0, length);
		return length;
	}
}
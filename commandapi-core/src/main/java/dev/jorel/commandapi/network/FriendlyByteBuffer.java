package dev.jorel.commandapi.network;

import com.google.common.primitives.Bytes;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Inspired by net.minecraft.network.FriendlyByteBuf

/**
 * A sequence of bytes that can be written to and read from.
 * <p>
 * Reading and writing is handled by two ints, the {@code readIndex} and {@code writeIndex}, which can be accessed using
 * {@link #getReadIndex()} and {@link #getWriteIndex()} respectively. These indices are automatically incremented when
 * reading and writing bytes. If the {@code readIndex} is ever greater than or equal to the {@code writeIndex} when a byte
 * is read, an {@link IllegalStateException} will be thrown for attempting to read out of bounds.
 * <p>
 * This class provides methods that make it easier to read and write certain data types, using methods like
 * {@link #readVarInt()} and {@link #writeVarInt(int)}. These methods handle formatting the bytes correctly, letting
 * developers simply worry about the structure of their data. If a specific method has a problem while reading bytes,
 * an appropriate {@link IllegalStateException} will be thrown to indicate something is misformatted.
 * <p>
 * This buffer can be created empty ({@link #FriendlyByteBuffer()}), or pre-written with a byte array
 * ({@link #FriendlyByteBuffer(byte[])}).
 */
public class FriendlyByteBuffer {
	private int readIndex = 0;
	private int writeIndex = 0;
	private final List<Byte> byteBuffer = new ArrayList<>();

	/**
	 * Creates a new {@link FriendlyByteBuffer} with no bytes, ready to be written to.
	 */
	public FriendlyByteBuffer() {
	}

	/**
	 * Creates a new {@link FriendlyByteBuffer} with the given bytes already written, ready to be read from.
	 *
	 * @param bytes The initial bytes to put in this buffer. This array is written to the buffer using
	 * {@link #writeBytes(byte...)}.
	 */
	public FriendlyByteBuffer(byte[] bytes) {
		this.writeBytes(bytes);
	}

	/////////////////////
	// Utility methods //
	/////////////////////

	/**
	 * Checks if the current {@code readIndex} for this buffer is out of bounds.
	 *
	 * @throws IllegalStateException If {@link #isReadIndexOutOfBounds()} returns true.
	 */
	public void checkReadIndexIsInBounds() throws IllegalStateException{
		this.checkReadIndexIsInBounds(this.readIndex);
	}

	/**
	 * Checks if the given {@code readIndex} is out of bounds.
	 *
	 * @param readIndex The index to check.
	 * @throws IllegalStateException If {@link #isReadIndexOutOfBounds(int)} returns true for the given {@code readIndex}.
	 */
	public void checkReadIndexIsInBounds(int readIndex) throws IllegalStateException{
		if(this.isReadIndexOutOfBounds(readIndex)) {
			throw new IllegalStateException("Read index (" + readIndex + ") cannot be " +
				(readIndex < 0 ? "negative" : "greater than or equal to write index (" + this.writeIndex + ")"));
		}
	}

	/**
	 * Evaluates if the current {@code readIndex} for this buffer is out of bounds.
	 *
	 * @return True if {@code this.readIndex < 0 || this.readIndex >= this.writeIndex}, and false otherwise.
	 */
	public boolean isReadIndexOutOfBounds() {
		return this.isReadIndexOutOfBounds(this.readIndex);
	}

	/**
	 * Evaluates if the given {@code readIndex} is out of bounds.
	 *
	 * @param readIndex The index to check.
	 * @return True if {@code readIndex < 0 || readIndex >= this.writeIndex}, and false otherwise.
	 */
	public boolean isReadIndexOutOfBounds(int readIndex) {
		return readIndex < 0 || readIndex >= this.writeIndex;
	}

	/**
	 * Checks if the current {@code writeIndex} for this buffer is out of bounds.
	 *
	 * @throws IllegalStateException If {@link #isWriteIndexOutOfBounds()} returns true.
	 */
	public void checkWriteIndexIsInBounds() throws IllegalStateException {
		this.checkWriteIndexIsInBounds(this.writeIndex);
	}

	/**
	 * Checks if the given {@code writeIndex} is out of bounds.
	 *
	 * @param writeIndex The index to check.
	 * @throws IllegalStateException If {@link #isWriteIndexOutOfBounds(int)} returns true for the given {@code writeIndex}.
	 */
	public void checkWriteIndexIsInBounds(int writeIndex) throws IllegalStateException{
		if(this.isWriteIndexOutOfBounds(writeIndex)) {
			throw new IllegalStateException("Write index (" + writeIndex + ") cannot be negative");
		}
	}

	/**
	 * Evaluates if the current {@code writeIndex} for this buffer is out of bounds.
	 *
	 * @return True if {@code this.writeIndex < 0}, and false otherwise.
	 */
	public boolean isWriteIndexOutOfBounds() {
		return this.isWriteIndexOutOfBounds(this.writeIndex);
	}

	/**
	 * Evaluates if the given {@code writeIndex} is out of bounds.
	 *
	 * @param writeIndex The index to check.
	 * @return True if {@code writeIndex < 0}, and false otherwise.
	 */
	public boolean isWriteIndexOutOfBounds(int writeIndex) {
		return writeIndex < 0;
	}

	/**
	 * Adds 0 to the bytes list until it reaches the write index.
	 * <p>
	 * This method handles the case where the write index might have moved ahead of the current size of the bytes list.
	 * For example:
	 * <pre>{@code
	 * this.bytes = [0, 1, 2] (size 3)
	 * this.writeIndex = 5
	 * }</pre>
	 * In order to add a byte at index 5, the bytes list must have at least size 5 according to the implementation of
	 * {@link ArrayList#add(int, Object)}. So, this method adds {@code this.writeIndex - this.bytes.size() = 5 - 3 = 2}
	 * padding zeros to the bytes list to get:
	 * <pre>{@code
	 * this.bytes = [0, 1, 2, 0, 0] (size 5)
	 * this.writeIndex = 5
	 * }</pre>
	 * We can now write a byte at index 5:
	 * <pre>{@code
	 * this.bytes = [0, 1, 2, 0, 0, 5] (size 6)
	 * this.writeIndex = 6
	 * }</pre>
	 * This method is automatically called when {@link #setWriteIndex(int)} is used. Anything else that might move the
	 * write index ahead of the size of the bytes list should call this method to ensure there isn't an
	 * {@link IndexOutOfBoundsException} when accessing bytes at the write index.
	 */
	private void padBytesWithZerosUntilWriteIndex() {
		if(this.writeIndex > this.byteBuffer.size()) {
			List<Byte> paddingZeros = Collections.nCopies(this.writeIndex - this.byteBuffer.size(), (byte) 0);
			this.byteBuffer.addAll(paddingZeros);
		}
	}

	private IllegalStateException unexpectedByteJustRead(String message) {
		return new IllegalStateException(
			message + "\n" +
			"At position " + (this.readIndex - 1) + "\n" +
			this.byteBuffer.subList(0, this.readIndex) + " <-- HERE " + this.byteBuffer.subList(this.readIndex, this.writeIndex)
		);
	}

	/////////////////////////
	// Getters and Setters //
	/////////////////////////

	/**
	 * @return The current {@code readIndex} for this buffer.
	 */
	public int getReadIndex() {
		return this.readIndex;
	}

	/**
	 * Sets the {@code readIndex} for this buffer.
	 * <p>
	 * This method checks the given index using {@link #isReadIndexOutOfBounds(int)} to make sure it is not out of bounds
	 * before setting the index of this buffer.
	 *
	 * @param index The new {@code readIndex}.
	 * @throws IllegalStateException If the given index is out of bounds.
	 */
	public void setReadIndex(int index) throws IllegalStateException{
		this.checkReadIndexIsInBounds(index);

		this.readIndex = index;
	}

	/**
	 * @return The current {@code writeIndex} for this buffer.
	 */
	public int getWriteIndex() {
		return this.writeIndex;
	}

	/**
	 * Sets the {@code writeIndex} for this buffer.
	 * <p>
	 * This method checks the given index using {@link #isWriteIndexOutOfBounds(int)} to make sure it is not out of bounds
	 * before setting the index of this buffer.
	 *
	 * @param index The new {@code writeIndex}.
	 * @throws IllegalStateException If the given index is out of bounds.
	 */
	public void setWriteIndex(int index) throws IllegalStateException {
		this.checkWriteIndexIsInBounds(index);

		this.writeIndex = index;

		this.padBytesWithZerosUntilWriteIndex();
	}

	/**
	 * Sets the {@code readIndex} and {@code writeIndex} to 0. This effectively clears this buffer, since future writes
	 * will overwrite the old bytes. The bytes are not actually forgotten, since if the write index is jumped forward, the
	 * bytes can still be read.
	 */
	public void resetIndices() {
		this.readIndex = 0;
		this.writeIndex = 0;
	}

	/**
	 * @return An array containing all the bytes written to this buffer.
	 * @throws IllegalStateException If the current {@code writeIndex} is out of bounds according to
	 * {@link #checkWriteIndexIsInBounds()}.
	 */
	public byte[] toByteArray() throws IllegalStateException {
		this.checkWriteIndexIsInBounds();

		return Bytes.toArray(this.byteBuffer.subList(0, this.writeIndex));
	}

	/**
	 * @return An array containing all the bytes left to be read from this buffer.
	 * @throws IllegalStateException If the current {@code writeIndex} is out of bounds according to
	 * {@link #checkWriteIndexIsInBounds()}, or if the current {@code readIndex} is out of bounds according to
	 * {@link #checkReadIndexIsInBounds()}.
	 */
	public byte[] getRemainingBytes() throws IllegalStateException {
		this.checkWriteIndexIsInBounds();
		this.checkReadIndexIsInBounds();

		return Bytes.toArray(this.byteBuffer.subList(this.readIndex, this.writeIndex));
	}

	/**
	 * @return The number of bytes currently written to this buffer.
	 */
	public int countTotalBytes() {
		return this.writeIndex;
	}

	/**
	 * @return The number of bytes left to read fom this buffer.
	 */
	public int countReadableBytes() {
		return this.writeIndex - this.readIndex;
	}

	////////////////////
	// Write and read //
	////////////////////

	/**
	 * Writes a single byte (8 bits) into this buffer.
	 *
	 * @param b The byte to write.
	 * @throws IllegalStateException If the {@code writeIndex} is out of bounds according to {@link #checkWriteIndexIsInBounds()}.
	 */
	public void writeByte(byte b) throws IllegalStateException {
		this.checkWriteIndexIsInBounds();

		if(this.writeIndex == this.byteBuffer.size()) {
			this.byteBuffer.add(b);
		} else {
			this.byteBuffer.set(this.writeIndex, b);
		}

		this.writeIndex++;
	}

	/**
	 * Writes a single byte (8 bits) into this buffer using {@link #writeByte(byte)}.
	 *
	 * @param i The byte to write, given as a 32-bit integer. Only the 8 least significant bits are written.
	 * @throws IllegalStateException If the write index goes out of bounds while writing the byte.
	 */
	public void writeByte(int i) throws IllegalStateException {
		this.writeByte((byte) i);
	}

	/**
	 * Reads a single byte (8 bits) from this buffer.
	 *
	 * @return The next byte in this buffer.
	 * @throws IllegalStateException If the {@code readIndex} is out of bounds according to {@link #checkReadIndexIsInBounds()}.
	 */
	public byte readByte() throws IllegalStateException{
		this.checkReadIndexIsInBounds();
		byte b = this.byteBuffer.get(this.readIndex);

		this.readIndex++;
		return b;
	}

	/**
	 * Writes multiple bytes to this buffer. This happens one at a time using {@link #writeByte(byte)}.
	 *
	 * @param bytes An array of bytes to write.
	 * @throws IllegalStateException If the write index goes out of bounds while writing bytes.
	 */
	public void writeBytes(byte... bytes) throws IllegalStateException {
		for(byte b : bytes) {
			this.writeByte(b);
		}
	}

	/**
	 * Reads {@code n} bytes from this buffer. This happens one at a time using {@link #readByte()}.
	 *
	 * @param n The number of bytes to read.
	 * @return An array of the next {@code n} bytes in this buffer.
	 * @throws IllegalStateException If the read index goes out of bounds while reading bytes.
	 */
	public byte[] readBytes(int n) throws IllegalStateException{
		// Verify we can actually read all these bytes before allocating the array
		this.checkReadIndexIsInBounds(this.readIndex + n - 1);

		byte[] bytes = new byte[n];
		for (int i = 0; i < n; i++) {
			bytes[i] = this.readByte();
		}
		return bytes;
	}

	/**
	 * Writes a 32 bit int to this buffer. This method writes the int as 4 bytes, starting with the 8 most significant
	 * bits.
	 *
	 * @param i The int to write.
	 * @throws IllegalStateException If the write index goes out of bounds while writing bytes.
	 */
	public void writeInt(int i) throws IllegalStateException {
		this.writeByte(i >>> 24);
		this.writeByte(i >>> 16);
		this.writeByte(i >>> 8);
		this.writeByte(i);
	}

	/**
	 * Reads a 32 bit int from this buffer. This method assumes the int was written by {@link #writeInt(int)}.
	 *
	 * @return The int read from this buffer.
	 * @throws IllegalStateException If the read index goes out of bounds while reading bytes.
	 */
	public int readInt() throws IllegalStateException{
		// I'm not entirely sure why, but we need to & each byte with 0xff, otherwise the | operations just return d
		//  Something to do with converting the bytes to signed ints?
		int a = (this.readByte() & 0xff) << 24;
		int b = (this.readByte() & 0xff) << 16;
		int c = (this.readByte() & 0xff) << 8;
		int d = (this.readByte() & 0xff);
		return a | b | c | d;
	}

	// (write/read)VarInt implementations adapted from https://wiki.vg/Protocol#VarInt_and_VarLong
	private static final int VAR_INT_CONTINUE_BIT = 0b10000000;
	private static final int VAR_INT_VALUE_BITS = 0b01111111;

	/**
	 * Writes a 32 bit int to this buffer using between 1 and 5 bytes, with small positive numbers using fewer bytes.
	 * For the details of this encoding, see <a href="https://wiki.vg/Protocol#VarInt_and_VarLong">VarInt and VarLong</a>.
	 *
	 * @param value The int to write.
	 * @throws IllegalStateException If the write index goes out of bounds while writing bytes.
	 */
	public void writeVarInt(int value) throws IllegalStateException{
		// Exit loop when removing the 7 least significant bits gives all 0s
		while ((value & ~VAR_INT_VALUE_BITS) != 0) {
			// Write the 7 least significant bits, plus set the highest bit to indicate int continues
			this.writeByte((value & VAR_INT_VALUE_BITS) | VAR_INT_CONTINUE_BIT);
			// Shift all bits right 7, including the sign bit
			value >>>= 7;
		}
		// Write the last 7 bits; the highest bit is 0 to show int stops
		this.writeByte(value);
	}

	/**
	 * Reads a 32 bit int from this buffer, encoded as a VarInt. This method assumes the VarInt was written by
	 * {@link #writeVarInt(int)}.
	 *
	 * @return The int read from this buffer.
	 * @throws IllegalStateException If the read index goes out of bounds while reading bytes or if the VarInt is improperly formatted.
	 */
	public int readVarInt() throws IllegalStateException{
		int value = 0;
		int bytes = 0;

		byte currentByte;
		do {
			bytes++;
			// A VarInt is maximum 5 bytes, otherwise the value does not fit in int
			//  If we get to more than 5 bytes, the VarInt is improperly formatted, and something has gone wrong
			if (bytes > 5) throw unexpectedByteJustRead("Expected VarInt to have no more than 5 bytes, but 5th byte had continue bit set");

			currentByte = this.readByte();

			// Add on the next 7 bits, shifting to the correct position based on current byte number
			value |= (currentByte & VAR_INT_VALUE_BITS) << ((bytes - 1) * 7);
			// Exit loop when the highest bit is 0 to show int stops
		} while ((currentByte & VAR_INT_CONTINUE_BIT) != 0);
		return value;
	}

	/**
	 * Writes a String to this buffer. The String is encoded as an array of bytes using the UTF-8 encoding.
	 * For the details of this encoding, see <a href="https://en.wikipedia.org/wiki/UTF-8">UTF-8 - Wikipedia</a>
	 *
	 * @param string The String to write.
	 * @param maxLength The length of the longest String that is allowed.
	 * @throws IllegalStateException If the given String is longer than the given maximum length.
	 */
	public void writeString(String string, int maxLength) {
		if (string.length() > maxLength) {
			throw new IllegalStateException("Given string (" + string.length() + " characters) is too long. " + maxLength + " characters maximum.");
		}

		byte[] bytes = string.getBytes(StandardCharsets.UTF_8);

		this.writeVarInt(bytes.length);
		this.writeBytes(bytes);
	}

	/**
	 * Reads a String from this buffer. This method assumes the String was written by {@link #writeString(String, int)}.
	 *
	 * @param maxLength The length of the longest String that is allowed.
	 * @return The String read from this buffer.
	 * @throws IllegalStateException If the read String is longer than the given maximum length.
	 */
	public String readString(int maxLength) throws IllegalStateException{
		int length = this.readVarInt();

		if (length > maxLength) {
			throw new IllegalStateException("Given string (" + length + " characters) is too long. " + maxLength + " characters maximum.");
		}

		byte[] bytes = this.readBytes(length);

		return new String(bytes, StandardCharsets.UTF_8);
	}
}

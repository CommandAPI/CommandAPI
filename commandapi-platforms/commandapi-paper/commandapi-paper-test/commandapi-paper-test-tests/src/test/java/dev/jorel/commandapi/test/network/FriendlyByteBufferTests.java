package dev.jorel.commandapi.test.network;

import dev.jorel.commandapi.network.FriendlyByteBuffer;
import dev.jorel.commandapi.test.MockPlatform;
import dev.jorel.commandapi.test.TestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class FriendlyByteBufferTests extends TestBase {

	/*********
	 * Setup *
	 *********/

	@BeforeEach
	public void setUp() {
		super.setUp();
	}

	@AfterEach
	public void tearDown() {
		super.tearDown();
	}

	/*********
	 * Tests *
	 *********/

	@Test
	void testReadIndexManipulation() {
		FriendlyByteBuffer buffer = new FriendlyByteBuffer(new byte[]{0, 1, 2, 3, 4});

		// Negative read index is out of bounds
		assertTrue(buffer.isReadIndexOutOfBounds(-1));
		assertThrowsWithMessage(
			IllegalStateException.class,
			"Read index (-1) cannot be negative",
			() -> buffer.checkReadIndexIsInBounds(-1)
		);
		assertThrowsWithMessage(
			IllegalStateException.class,
			"Read index (-1) cannot be negative",
			() -> buffer.setReadIndex(-1)
		);
		MockPlatform.setField(FriendlyByteBuffer.class, "readIndex", buffer, -1); // Force index out of bounds
		assertTrue(buffer.isReadIndexOutOfBounds());
		assertThrowsWithMessage(
			IllegalStateException.class,
			"Read index (-1) cannot be negative",
			buffer::checkReadIndexIsInBounds
		);

		// Read index greater than or equal to write index (5) is out of bounds
		assertTrue(buffer.isReadIndexOutOfBounds(5));
		assertTrue(buffer.isReadIndexOutOfBounds(6));
		assertThrowsWithMessage(
			IllegalStateException.class,
			"Read index (6) cannot be greater than or equal to write index (5)",
			() -> buffer.checkReadIndexIsInBounds(6)
		);
		assertThrowsWithMessage(
			IllegalStateException.class,
			"Read index (6) cannot be greater than or equal to write index (5)",
			() -> buffer.setReadIndex(6)
		);
		MockPlatform.setField(FriendlyByteBuffer.class, "readIndex", buffer, 6); // Force index out of bounds
		assertTrue(buffer.isReadIndexOutOfBounds());
		assertThrowsWithMessage(
			IllegalStateException.class,
			"Read index (6) cannot be greater than or equal to write index (5)",
			buffer::checkReadIndexIsInBounds
		);

		// Values in between are okay
		buffer.setReadIndex(2);

		assertEquals(2, buffer.getReadIndex());
		assertEquals(3, buffer.countReadableBytes());
		assertArrayEquals(new byte[]{2, 3, 4}, buffer.getRemainingBytes());

		assertEquals(2, buffer.readByte());

		assertEquals(3, buffer.getReadIndex());
		assertEquals(2, buffer.countReadableBytes());
		assertArrayEquals(new byte[]{3, 4}, buffer.getRemainingBytes());
	}

	@Test
	void testWriteIndexManipulation() {
		FriendlyByteBuffer buffer = new FriendlyByteBuffer();

		// Negative write index is out of bounds
		assertTrue(buffer.isWriteIndexOutOfBounds(-1));
		assertThrowsWithMessage(
			IllegalStateException.class,
			"Write index (-1) cannot be negative",
			() -> buffer.checkWriteIndexIsInBounds(-1)
		);
		assertThrowsWithMessage(
			IllegalStateException.class,
			"Write index (-1) cannot be negative",
			() -> buffer.setWriteIndex(-1)
		);
		MockPlatform.setField(FriendlyByteBuffer.class, "writeIndex", buffer, -1); // Force index out of bounds
		assertTrue(buffer.isWriteIndexOutOfBounds());
		assertThrowsWithMessage(
			IllegalStateException.class,
			"Write index (-1) cannot be negative",
			buffer::checkWriteIndexIsInBounds
		);

		// Check 0 padding of byte list
		buffer.setWriteIndex(5);
		assertEquals(5, buffer.getWriteIndex());
		assertEquals(5, buffer.countTotalBytes());
		assertArrayEquals(new byte[]{0, 0, 0, 0, 0}, buffer.toByteArray());

		// Check over-writing
		buffer.setWriteIndex(3);
		buffer.writeByte(1);
		assertEquals(4, buffer.getWriteIndex());
		assertEquals(4, buffer.countTotalBytes());
		assertArrayEquals(new byte[]{0, 0, 0, 1}, buffer.toByteArray());
	}

	@Test
	void testReadWriteByte() {
		FriendlyByteBuffer buffer = new FriendlyByteBuffer();

		// Write a byte and it should be read back
		buffer.writeByte((byte) 0);
		assertEquals((byte) 0, buffer.readByte());

		buffer.writeByte((byte) 10);
		assertEquals((byte) 10, buffer.readByte());

		buffer.writeByte((byte) 100);
		assertEquals((byte) 100, buffer.readByte());

		buffer.writeByte((byte) 0);
		buffer.writeByte((byte) 1);
		buffer.writeByte((byte) 2);
		assertEquals((byte) 0, buffer.readByte());
		assertEquals((byte) 1, buffer.readByte());
		assertEquals((byte) 2, buffer.readByte());

		// Writing as an int also works
		buffer.writeByte(0);
		assertEquals(0, buffer.readByte());

		buffer.writeByte(10);
		assertEquals(10, buffer.readByte());

		buffer.writeByte(100);
		assertEquals(100, buffer.readByte());

		buffer.writeByte(0);
		buffer.writeByte(1);
		buffer.writeByte(2);
		assertEquals(0, buffer.readByte());
		assertEquals(1, buffer.readByte());
		assertEquals(2, buffer.readByte());

		// Writing an int chops off anything above the lowest 8 bits
		buffer.writeByte(0xff00);
		assertEquals(0x00, buffer.readByte());

		buffer.writeByte(0xffaa);
		assertEquals((byte) 0xaa, buffer.readByte());

		buffer.writeByte(0xffffff12);
		assertEquals((byte) 0x12, buffer.readByte());

		// Nothing left in the buffer
		assertEquals(0, buffer.countReadableBytes());


		// Reading when there is nothing written goes out of bounds
		assertThrowsWithMessage(
			IllegalStateException.class,
			"Read index (15) cannot be greater than or equal to write index (15)",
			buffer::readByte
		);
	}

	@Test
	void testReadWriteBytes() {
		FriendlyByteBuffer buffer = new FriendlyByteBuffer();

		// Write multiple bytes and they should be read back
		buffer.writeBytes(new byte[]{1, 2, 3, 4});
		assertArrayEquals(new byte[]{1, 2, 3, 4}, buffer.readBytes(4));

		buffer.writeBytes(new byte[]{4, 3, 2, 1});
		assertArrayEquals(new byte[]{4, 3, 2, 1}, buffer.readBytes(4));

		buffer.writeBytes(new byte[]{1, 2, 3, 4, 5, 6});
		assertArrayEquals(new byte[]{1, 2, 3, 4, 5, 6}, buffer.readBytes(6));

		buffer.writeBytes(new byte[]{1, 2, 3, 4});
		assertArrayEquals(new byte[]{1, 2}, buffer.readBytes(2));
		assertArrayEquals(new byte[]{3, 4}, buffer.readBytes(2));

		// Nothing left in the buffer
		assertEquals(0, buffer.countReadableBytes());


		// Reading when there is nothing written goes out of bounds
		assertThrowsWithMessage(
			IllegalStateException.class,
			"Read index (18) cannot be greater than or equal to write index (18)",
			() -> buffer.readBytes(1)
		);

		// Reading more bytes than written goes out of bounds
		buffer.writeBytes(new byte[]{1, 2, 3, 4});
		assertThrowsWithMessage(
			IllegalStateException.class,
			"Read index (22) cannot be greater than or equal to write index (22)",
			() -> buffer.readBytes(5)
		);
	}

	@Test
	void testReadWriteInt() {
		FriendlyByteBuffer buffer = new FriendlyByteBuffer();

		// An int should be written as 4 bytes, and it should be read back
		buffer.writeInt(0);
		assertArrayEquals(new byte[]{0, 0, 0, 0}, buffer.getRemainingBytes());
		assertEquals(0, buffer.readInt());

		buffer.writeInt(10);
		assertArrayEquals(new byte[]{0, 0, 0, 10}, buffer.getRemainingBytes());
		assertEquals(10, buffer.readInt());

		buffer.writeInt(1);
		buffer.writeInt(1 << 8);
		buffer.writeInt(1 << 16);
		buffer.writeInt(1 << 24);
		assertArrayEquals(new byte[]{
				0, 0, 0, 1,
				0, 0, 1, 0,
				0, 1, 0, 0,
				1, 0, 0, 0},
			buffer.getRemainingBytes()
		);
		assertEquals(1, buffer.readInt());
		assertEquals(1 << 8, buffer.readInt());
		assertEquals(1 << 16, buffer.readInt());
		assertEquals(1 << 24, buffer.readInt());

		buffer.writeInt(0x01234567);
		assertArrayEquals(new byte[]{0x01, 0x23, 0x45, 0x67}, buffer.getRemainingBytes());
		assertEquals(0x01234567, buffer.readInt());

		buffer.writeInt(0xaabbccdd);
		assertArrayEquals(new byte[]{(byte) 0xaa, (byte) 0xbb, (byte) 0xcc, (byte) 0xdd}, buffer.getRemainingBytes());
		assertEquals(0xaabbccdd, buffer.readInt());

		// Nothing left in the buffer
		assertEquals(0, buffer.countReadableBytes());
	}

	@Test
	void testReadWriteVarInt() {
		FriendlyByteBuffer buffer = new FriendlyByteBuffer();

		// A VarInt should be written as 1 to 5 bytes, and it should be read back
		// Examples copied from "Sample VarInts" at https://wiki.vg/Protocol#VarInt_and_VarLong
		buffer.writeVarInt(0);
		assertArrayEquals(new byte[]{0}, buffer.getRemainingBytes());
		assertEquals(0, buffer.readVarInt());

		buffer.writeVarInt(1);
		assertArrayEquals(new byte[]{1}, buffer.getRemainingBytes());
		assertEquals(1, buffer.readVarInt());

		buffer.writeVarInt(2);
		assertArrayEquals(new byte[]{2}, buffer.getRemainingBytes());
		assertEquals(2, buffer.readVarInt());


		buffer.writeVarInt(127);
		assertArrayEquals(new byte[]{0x7f}, buffer.getRemainingBytes());
		assertEquals(127, buffer.readVarInt());

		buffer.writeVarInt(128);
		assertArrayEquals(new byte[]{(byte) 0x80, 0x01}, buffer.getRemainingBytes());
		assertEquals(128, buffer.readVarInt());

		buffer.writeVarInt(255);
		assertArrayEquals(new byte[]{(byte) 0xff, 0x01}, buffer.getRemainingBytes());
		assertEquals(255, buffer.readVarInt());


		buffer.writeVarInt(25565);
		assertArrayEquals(new byte[]{(byte) 0xdd, (byte) 0xc7, 0x01}, buffer.getRemainingBytes());
		assertEquals(25565, buffer.readVarInt());

		buffer.writeVarInt(2097151);
		assertArrayEquals(new byte[]{(byte) 0xff, (byte) 0xff, 0x7f}, buffer.getRemainingBytes());
		assertEquals(2097151, buffer.readVarInt());

		
		buffer.writeVarInt(2147483647);
		assertArrayEquals(new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x07}, buffer.getRemainingBytes());
		assertEquals(2147483647, buffer.readVarInt());

		buffer.writeVarInt(-1);
		assertArrayEquals(new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x0f}, buffer.getRemainingBytes());
		assertEquals(-1, buffer.readVarInt());

		buffer.writeVarInt(-2147483648);
		assertArrayEquals(new byte[]{(byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, 0x08}, buffer.getRemainingBytes());
		assertEquals(-2147483648, buffer.readVarInt());

		// Nothing left in the buffer
		assertEquals(0, buffer.countReadableBytes());


		buffer.resetIndices();

		// Reading error when VarInt exceeds 5 bytes
		buffer.writeBytes((byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80);
		assertThrowsWithMessage(
			IllegalStateException.class,
			"""
				Expected VarInt to have no more than 5 bytes, but 5th byte had continue bit set
				At position 4
				[-128, -128, -128, -128, -128] <-- HERE [-128, -128, -128]""",
			buffer::readVarInt
		);

		// Reading goes out of bounds if continue bit is set at end of buffer
		assertThrowsWithMessage(
			IllegalStateException.class,
			"Read index (8) cannot be greater than or equal to write index (8)",
			buffer::readVarInt
		);
	}

	@Test
	void testReadReadWriteString() {
		FriendlyByteBuffer buffer = new FriendlyByteBuffer();

		// A String should be written as array of bytes represents each character as its UTF-8 encoded bytes in order,
		//  preceded by the length of that byte array as a VarInt

		// Ascii chars are nice because they are directly encoded
		buffer.writeString("Hello World", 100);
		assertArrayEquals(new byte[]{11, 'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd'}, buffer.getRemainingBytes());
		assertEquals("Hello World", buffer.readString(100));

		// Larger Unicode chars take up more bytes
		//  Example characters from https://en.wikipedia.org/wiki/UTF-8#Encoding_process
		buffer.writeString("$£€\uD800\uDF48", 100);
		assertArrayEquals(new byte[]{10,
			/*$*/ 0x24,
			/*£*/ (byte) 0xc2, (byte) 0xa3,
			/*€*/ (byte) 0xe2, (byte) 0x82, (byte) 0xac,
			/*𐍈*/ (byte) 0xf0, (byte) 0x90, (byte) 0x8d, (byte) 0x88},
			buffer.getRemainingBytes()
		);
		assertEquals("$£€\uD800\uDF48", buffer.readString(100));

		// Long strings use multiple bytes for their length
		String long128String = " ".repeat(128);
		byte[] long128Bytes = new byte[130];
		Arrays.fill(long128Bytes, (byte) ' ');
		// Length (128) encoded as VarInt in 2 bytes
		long128Bytes[0] = (byte) 0x80;
		long128Bytes[1] = 0x01;

		buffer.writeString(long128String,1000);
		assertArrayEquals(long128Bytes, buffer.getRemainingBytes());
		assertEquals(long128String, buffer.readString(1000));

		// Nothing left in the buffer
		assertEquals(0, buffer.countReadableBytes());


		buffer.resetIndices();

		// Reading goes out of bounds if the length byte is larger than actual number of bytes
		buffer.writeBytes(new byte[]{5, 1, 2, 3});
		assertThrowsWithMessage(
			IllegalStateException.class,
			"Read index (4) cannot be greater than or equal to write index (4)",
			() -> buffer.readString(100)
		);
	}
}

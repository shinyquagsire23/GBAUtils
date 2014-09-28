package org.zzl.minegaming.GBAUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class NewLz77
{
	public enum CheckLz77Type
	{
		Sprite, Palette
	}

	// For picking what type of Compression Look-up we want
	public enum CompressionMode
	{
		Old, // Good
		New // Perfect!
	}

	public static int getLz77DataLength(GBARom rom, int offset)
	{
		byte[] Data = rom.readBytes(offset, 0x10);
		return (int) BitConverter.ToInt32(new byte[] { Data[1], Data[2], Data[3], 0x0 });
	}

	public static int getLz77DataLength(byte[] rom, int offset)
	{
		byte[] Data = BitConverter.GrabBytes(rom, offset, 0x10);
		return (int) BitConverter.ToInt32(new byte[] { Data[1], Data[2], Data[3], 0x0 });
	}

	public static byte[] compressLZ10(byte[] indata)
	{
		ByteArrayOutputStream outstream = new ByteArrayOutputStream();
		int inLength = indata.length;
		if (inLength > 0xFFFFFF)
			return null;

		// write the compression header first
		outstream.write(0x10);
		outstream.write((byte) (inLength & 0xFF));
		outstream.write((byte) ((inLength >> 8) & 0xFF));
		outstream.write((byte) ((inLength >> 16) & 0xFF));

		int compressedLength = 4;

		// we do need to buffer the output, as the first byte indicates which
		// blocks are compressed.
		// this version does not use a look-ahead, so we do not need to buffer
		// more than 8 blocks at a time.
		byte[] outbuffer = new byte[8 * 2 + 1];
		outbuffer[0] = 0;
		int bufferlength = 1, bufferedBlocks = 0;
		int readBytes = 0;
		while (readBytes < inLength)
		{
			// we can only buffer 8 blocks at a time.
			if (bufferedBlocks == 8)
			{
				outstream.write(outbuffer, 0, bufferlength);
				compressedLength += bufferlength;
				// reset the buffer
				outbuffer[0] = 0;
				bufferlength = 1;
				bufferedBlocks = 0;
			}

			// determine if we're dealing with a compressed or raw block.
			// it is a compressed block when the next 3 or more bytes can be
			// copied from
			// somewhere in the set of already compressed bytes.
			int[] dispArr = new int[1];
			int oldLength = Math.min(readBytes, 0x1000);
			int length = GetOccurrenceLength(indata, readBytes, (int) Math.min(inLength - readBytes, 0x12), readBytes - oldLength, oldLength, dispArr);
			int disp = dispArr[0];

			// length not 3 or more? next byte is raw data
			if (length < 3)
			{
				outbuffer[bufferlength++] = indata[readBytes++];
			}
			else
			{
				// 3 or more bytes can be copied? next (length) bytes will be
				// compressed into 2 bytes
				readBytes += length;

				// mark the next block as compressed
				outbuffer[0] |= (byte) (1 << (7 - bufferedBlocks));

				outbuffer[bufferlength] = (byte) (((length - 3) << 4) & 0xF0);
				outbuffer[bufferlength] |= (byte) (((disp - 1) >> 8) & 0x0F);
				bufferlength++;
				outbuffer[bufferlength] = (byte) ((disp - 1) & 0xFF);
				bufferlength++;
			}
			bufferedBlocks++;
		}

		// copy the remaining blocks to the output
		if (bufferedBlocks > 0)
		{
			outstream.write(outbuffer, 0, bufferlength);
			compressedLength += bufferlength;
			// make the compressed file 4-byte aligned.
			while ((compressedLength % 4) != 0)
			{
				outstream.write(0);
				compressedLength++;
			}
		}

		return outstream.toByteArray();
	}

	public static int GetOccurrenceLength(byte[] indata, int newPtr, int newLength, int oldPtr, int oldLength, int[] disp)
	{
		int minDisp = 1;
		disp[0] = 0;
		if (newLength == 0)
			return 0;
		int maxLength = 0;
		// try every possible 'disp' value (disp = oldLength - i)
		for (int i = 0; i < oldLength - minDisp; i++)
		{
			// work from the start of the old data to the end, to mimic the
			// original implementation's behaviour
			// (and going from start to end or from end to start does not
			// influence the compression ratio anyway)
			int currentOldStart = oldPtr + i;
			int currentLength = 0;
			// determine the length we can copy if we go back (oldLength - i)
			// bytes
			// always check the next 'newLength' bytes, and not just the
			// available 'old' bytes,
			// as the copied data can also originate from what we're currently
			// trying to compress.
			for (int j = 0; j < newLength; j++)
			{
				// stop when the bytes are no longer the same
				if (indata[currentOldStart + j] != indata[newPtr + j])
					break;
				currentLength++;
			}

			// update the optimal value
			if (currentLength > maxLength)
			{
				maxLength = currentLength;
				disp[0] = oldLength - i;

				// if we cannot do better anyway, stop trying.
				if (maxLength == newLength)
					break;
			}
		}
		return maxLength;
	}
}

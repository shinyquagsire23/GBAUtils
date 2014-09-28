package org.zzl.minegaming.GBAUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import dsdecmp.*;

/**
 * Wrapper of debloat's Lz77 algorithm adapted for purposes within GBA ROM Hacking
 * @author maxamillion
 *
 */
public class Lz77
{
	public static int getUncompressedSize(GBARom ROM, int offset)
	{
		return NewLz77.getLz77DataLength(ROM, offset);
	}
	
	public static int[] decompressLZ77(GBARom ROM, int offset)
	{
		InputStream stream = new ByteArrayInputStream(ROM.getData());
		HexInputStream hexstream = new HexInputStream(stream);
		
		try
		{
			stream.skip(offset);
			return Compression.Decompress(hexstream);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
		/*try
		{
			return BitConverter.ToInts(NewLz77.DecompressBytes(ROM.readBytes(offset, NewLz77.getLz77DataLength(ROM, offset))));
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new int[]{0};
		}*/
	}
	
	public static int[] decompressLZ77(byte[] ROM, int offset)
	{
		InputStream stream = new ByteArrayInputStream(ROM);
		HexInputStream hexstream = new HexInputStream(stream);
		
		try
		{
			stream.skip(offset);
			return Compression.Decompress(hexstream);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
		/*try
		{
			return BitConverter.ToInts(NewLz77.DecompressBytes(BitConverter.GrabBytes(ROM, offset, NewLz77.getLz77DataLength(ROM, offset))));
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new int[]{0};
		}*/
	}
	
	public static int[] compressLZ77(byte[] data)
	{
		byte[] bytes = null;
		try
		{
			bytes = NewLz77.compressLZ10(data);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		return BitConverter.ToInts(bytes);
	}
	
	public static byte[] compressLZ77(int[] dats)
	{
		byte[] bytes = null;
		try
		{
			bytes = NewLz77.compressLZ10(BitConverter.toBytes(dats));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		return bytes;
	}
}

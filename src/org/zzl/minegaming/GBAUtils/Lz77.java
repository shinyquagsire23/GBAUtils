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
			return new int[] {0};
		}
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
			return new int[] {0};
		}
	}
	
	public static int[] compressLZ77(byte[] data)
	{
		String decompressed = new String(data);
		String compressed = dsdecmp.Lz77.compressStr(decompressed);
		byte[] bytes = compressed.getBytes();
		int[] ints = new int[bytes.length];
		int i = 0;
		for(byte b : bytes)
		{
			ints[i] = (int)(b & 0xFF);
			i++;
		}
		return ints;
	}
	
	public static byte[] compressLZ77(int[] dats)
	{
		byte[] data = new byte[dats.length];
		for(int i = 0; i < dats.length; i++)
		{
			data[i] = (byte)(dats[i] - 128);
		}
		
		String decompressed = new String(data);
		String compressed = dsdecmp.Lz77.compressStr(decompressed);
		byte[] bytes = compressed.getBytes();
		
		return bytes;
	}
}

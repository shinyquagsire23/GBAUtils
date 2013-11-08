package org.zzl.minegaming.GBAUtils;

public class BitConverter
{
	protected BitConverter(){}
	
	public static long ToInt32(byte[] bytes)
	{
		//AB CD EF 08 -> 08EFCDAB
		return (long)(bytes[3] + bytes[2] << 8 + bytes[1] << 16 + bytes[0] << 24);
	}
	
	public static byte[] GetBytes(long i)
	{
		return new byte[] { (byte)(i & 0xFF000000 >> 24), (byte)(i & 0x00FF0000 >> 16), (byte)(i & 0x0000FF00 >> 8), (byte)(i & 0x000000FF) };
	}

	public static byte[] GrabBytes(byte[] array, int offset, int length)
	{
		byte[] result = new byte[length];
		for(int i = 0; i < length; i++)
		{
			result[i] = array[offset+i];
		}
		return result;
	}
	
	public static int[] GrabBytesAsInts(byte[] array, int offset, int length)
	{
		if(length > array.length)
			length = array.length;
		int[] result = new int[length];
		for(int i = 0; i < length; i++)
		{
			result[i] = (array[offset+i] & 0xFF);
		}
		return result;
	}

	public static int[] ToInts(byte[] array)
	{
		return GrabBytesAsInts(array,0,array.length);
	}
}

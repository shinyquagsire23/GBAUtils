package org.zzl.minegaming.GBAUtils;

public class BytesAndIndexAndBestLength
{
	private byte[] bytes;
	private int index;
	private int bestlength;

	public BytesAndIndexAndBestLength(byte[] bytes, int index, int bestlength)
	{
		this.bytes = bytes;
		this.index = index;
	}

	public byte[] getBytes()
	{
		return bytes;
	}

	public int getIndex()
	{
		return index;
	}

	public int getBestLength()
	{
		return bestlength;
	}

	public void setBytes(byte[] bytes)
	{
		this.bytes = bytes;
	}

	public void setIndex(int index)
	{
		this.index = index;
	}

	public void setBestLength(int bestlength)
	{
		this.bestlength = bestlength;
	}
}

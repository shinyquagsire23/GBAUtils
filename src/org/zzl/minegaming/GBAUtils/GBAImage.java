package org.zzl.minegaming.GBAUtils;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;

public class GBAImage
{
	private Palette p;
	private int[] data;
	private Point size;
	public GBAImage(int[] imageBytes, Palette palette, Point size)
	{
		p = palette;
		data = imageBytes;
		this.size = size;
	}
	
	public BufferedImage getBufferedImage()
	{
		if(p.getSize() == 16)
			return get16Image();
		else
			return get256Image();
	}
	
	private BufferedImage get16Image()
	{
		BufferedImage im = new BufferedImage(size.x, size.y, BufferedImage.TYPE_INT_RGB);
		Graphics g = im.getGraphics();
		int x = -1;
		int y = 0;
		int blockx = 0;
		int blocky = 0;
		for(int i = 0; i < data.length * 2; i++)
		{
			x++;
			if(x >= 8)
			{
				x = 0;
				y++;
			}
			if(y >= 8)
			{
				y = 0;
				blockx++;
			}
			if(blockx > (im.getWidth() / 8) - 1)
			{
				blockx = 0;
				blocky++;
			}
			
			int pal = data[i/2];
			if((i & 1) == 0)
				pal &= 0xF;
			else
				pal = (pal & 0xF0) >> 4;

			try
			{
				g.setColor(p.getIndex(pal));
				g.drawRect(x + (blockx * 8), y + (blocky * 8), 1, 1);
			}
			catch(Exception e){}
		}
		return im;
	}
	
	private BufferedImage get256Image()
	{
		BufferedImage im = new BufferedImage(size.x, size.y, BufferedImage.TYPE_INT_RGB);
		Graphics g = im.getGraphics();
		int x = -1;
		int y = 0;
		int blockx = 0;
		int blocky = 0;
		for(int i = 0; i < data.length; i++)
		{
			x++;
			if(x >= 8)
			{
				x = 0;
				y++;
			}
			if(y >= 8)
			{
				y = 0;
				blockx++;
			}
			if(blockx > (im.getWidth() / 8) - 1)
			{
				blockx = 0;
				blocky++;
			}
			
			int pal = data[i];
			try
			{
				g.setColor(p.getIndex(pal));
				g.drawRect(x + (blockx * 8), y + (blocky * 8), 1, 1);
			}
			catch(Exception e){}
		}
		return im;
	}
	
	public int[] getRaw()
	{
		return data;
	}
}

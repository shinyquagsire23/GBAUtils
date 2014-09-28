package org.zzl.minegaming.GBAUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;

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
		//make sure verything is dandy..
	
	}
	
	public static GBAImage fromImage(Image img, Palette p)
	{
		BufferedImage im = (BufferedImage)img;
		int x = -1;
		int y = 0;
		int blockx = 0;
		int blocky = 0;
		int[] data = new int[(im.getWidth() * im.getHeight()) / 2];
		for(int i = 0; i < im.getWidth() * im.getHeight(); i++)
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
			Color c = new Color(im.getRGB(x + (blockx * 8), y + (blocky * 8)), true);
			int pal = 0;
			for(int j = 0; j < 16; j++) 
			{
				Color col = p.getIndex(j);
				if(col.equals(c))
				{
					pal = j;
				}
			}
			
			int toWrite = data[i/2];
			if((i & 1) == 0)
				toWrite |= (pal & 0xF);
			else
				toWrite |= ((pal << 4) & 0xF0);
			
			data[i/2] = toWrite;
		}
		return new GBAImage(data, p, new Point(img.getWidth(null), img.getHeight(null)));
	}
	
	public BufferedImage getBufferedImage()
	{
		return getBufferedImage(true);
	}
	
	public BufferedImage getBufferedImage(boolean transparency)
	{
		if(p.getSize() == 16)
			return get16Image(p, transparency);
		else
			return get256Image(transparency);
	}
	
	public BufferedImage getBufferedImageFromPal(Palette pl)
	{
		return get16Image(pl, true);
	}
	
	public BufferedImage getBufferedImageFromPal(Palette pl, boolean trans)
	{
		return get16Image(pl, trans);
	}
	
	private BufferedImage get16Image(Palette pl, boolean transparency)
	{
		BufferedImage im = new BufferedImage(size.x, size.y, BufferedImage.TYPE_INT_ARGB);
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
				im.getRaster().setPixel(x + (blockx * 8), y + (blocky * 8), new int[]{pl.getIndex(pal).getRed(),pl.getIndex(pal).getGreen(),pl.getIndex(pal).getBlue(),(transparency && pal == 0 ? 0 : 255)});
			}
			catch(Exception e){}
		}
		return im;
	}
	
	private BufferedImage get256Image(boolean transparency)
	{
		BufferedImage im = new BufferedImage(size.x, size.y, BufferedImage.TYPE_INT_ARGB);
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
				g.setColor( p.getIndex(pal));
				g.drawRect(x + (blockx * 8), y + (blocky * 8), 1, 1);
			}
			catch(Exception e){}
		}
		return im;
	}
	
	public BufferedImage getIndexedImage()
	{
		return getIndexedImage(p, true);
	}
	
	public BufferedImage getIndexedImage(Palette pl, boolean transparency)
	{		
		IndexColorModel icm = new IndexColorModel(8,16,pl.getReds(),pl.getGreens(),pl.getBlues(), 0);
		BufferedImage indexedImage = new BufferedImage(size.x, size.y, BufferedImage.TYPE_BYTE_INDEXED, icm);
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
			if(blockx > (indexedImage.getWidth() / 8) - 1)
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
				indexedImage.getRaster().getDataBuffer().setElem((x + (blockx * 8))+((y + (blocky * 8)) * indexedImage.getWidth()), pal);
			}
			catch(Exception e){}
		}
		return indexedImage;
	}
	
 	public int[] getRaw()
	{
		return data;
	}
}

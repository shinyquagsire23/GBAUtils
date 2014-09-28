package org.zzl.minegaming.GBAUtils;

import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import java.awt.BorderLayout;
import java.awt.Image;

public class PictureFrame extends JFrame
{
	public static boolean isOpen = false;
	
	/**
	 * Serial Stuffs
	 */
	private static final long serialVersionUID = -5311502864811966111L;

	public PictureFrame(BufferedImage img)
	{
		ImagePanel panel = new ImagePanel(img);
		getContentPane().add(panel, BorderLayout.CENTER);
		this.setSize(img.getWidth(), 256);
	}
	

	public PictureFrame(Image img)
	{
		ImagePanel panel = new ImagePanel(img);
		getContentPane().add(panel, BorderLayout.CENTER);
		this.setSize(img.getWidth(null), 256);
	}
}

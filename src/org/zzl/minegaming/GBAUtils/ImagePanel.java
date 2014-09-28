package org.zzl.minegaming.GBAUtils;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ImagePanel extends JPanel{

	private static final long serialVersionUID = -877213633894324075L;
	private Image image;

    public ImagePanel(BufferedImage img) 
    {
       image = img;
    }
    
    public ImagePanel(Image img)
    {
    	image = img;
    }

    public void setImage(BufferedImage img)
    {
    	image = img;
    }
    
    public void setImage(Image img)
    {
    	image = img;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null); // see javadoc for more info on the parameters            
    }

}

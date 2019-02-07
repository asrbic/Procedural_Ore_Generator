package map;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class MapData {
	
	public static final int FRONT = 0;
	public static final int LEFT = 1;
	public static final int RIGHT = 2;
	public static final int UP = 3;
	public static final int DOWN = 4;
	public static final int BACK = 5;
	
	public static final int MAP_SIDES = 6;
	public static final int[][] adjacency = 
		{{},
			{},
			{},
			{},
			{},
			{}};
	
	public BufferedImage[] images;
	public BufferedImage[] surfaceHintImages;
	public BufferedImage[] colouredMaps;
	
	public static final int BACKGROUND_ORE_COLOUR = 255;
	public static final int BACKGROUND_ADD_COLOUR = 0xFF000000;

	int mapSize;
	
	public MapData() {
		images = new BufferedImage[MAP_SIDES];
		surfaceHintImages = new BufferedImage[MAP_SIDES];
		colouredMaps = new BufferedImage[MAP_SIDES];
	}
	
	public void clearOreData() {
		for(int i = 0; i < MAP_SIDES; ++i) {
			BufferedImage img = images[i];
			if(img != null) {
				for(int j = 0; j < img.getWidth(); ++j) {
					for(int k = 0; k < img.getHeight(); ++k) {
						img.setRGB(j, k, img.getRGB(j, k) | BACKGROUND_ORE_COLOUR);
					}
				}
			}
			BufferedImage hintImg = surfaceHintImages[i];
			if(hintImg != null) {
				Graphics2D    graphics = img.createGraphics();
				graphics.setPaint ( new Color(BACKGROUND_ADD_COLOUR));
				graphics.fillRect (0, 0, hintImg.getWidth(), hintImg.getHeight());
			}
			BufferedImage colouredImg = colouredMaps[i];
			if(colouredImg != null) {
				for(int j = 0; j < colouredImg.getWidth(); ++j) {
					for(int k = 0; k < colouredImg.getHeight(); ++k) {
						colouredImg.setRGB(j, k, colouredImg.getRGB(j, k) | BACKGROUND_ORE_COLOUR);
					}
				}
			}
		}
	}
	
	public int getMapSize() {
		return mapSize;
	}
	
	
	public void setMapSize(int mapSize) {
		this.mapSize = mapSize;
	}
	
	public void calculateMapSize() {
		if(this.images.length > 0) {
			if(this.images[0] != null) {
				//assume that all mapes are the same size and are square
				mapSize = this.images[0].getWidth();
			}
		}
	}
}

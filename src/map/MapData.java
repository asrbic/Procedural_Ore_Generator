package map;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import config.CommonConfig.PlanetFace;

public class MapData {
	public static final Logger logger = LogManager.getLogger("MapData");
	
	public BufferedImage[] images;
	public BufferedImage[] surfaceHintImages;
	public BufferedImage[] colouredMaps;
	
	public static final int BACKGROUND_ORE_COLOUR = 255;
	public static final int BACKGROUND_ADD_COLOUR = 0xFF000000;
	public static final int ORE_EXCLUDER = 0xFFFFFF00;
	public static final int ORE_FILTER = 0x000000FF;
	public static final int ICE_FILTER = 0x00520000; //82 (colour in R channel for ice in hex)
	public static final int OPAQUE = 0xFF000000;
	public static final int UNSET_ORE_FILTER = 0x000000FF;

	int mapSize;
	
	public MapData() {
		int mapSides = PlanetFace.ALL.length;
		images = new BufferedImage[mapSides];
		surfaceHintImages = new BufferedImage[mapSides];
		colouredMaps = new BufferedImage[mapSides];
	}
	
	public void clearOreData() {
		for(int i = 0; i < images.length; ++i) {
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
				Graphics2D graphics = hintImg.createGraphics();
				graphics.setPaint ( new Color(BACKGROUND_ADD_COLOUR));
				graphics.fillRect (0, 0, hintImg.getWidth(), hintImg.getHeight());
			}
			BufferedImage colouredImg = colouredMaps[i];
			if(colouredImg != null) {
				for(int j = 0; j < colouredImg.getWidth(); ++j) {
					for(int k = 0; k < colouredImg.getHeight(); ++k) {
						int pixRGB = colouredImg.getRGB(j, k) & ORE_EXCLUDER;
						//convert coloured test image to greyscale
						int rgb[] = new int[] {
						        (pixRGB >> 16) & 0xff, //red
						        (pixRGB >>  8) & 0xff, //green
						        (pixRGB      ) & 0xff  //blue
						};
						int avg = (( rgb[0] + rgb[1] + rgb[2]) / 3);
					    int grey_rgb = 0;
					    grey_rgb |= avg;
					    for(int l = 0; l < 3; l++) {
					    	grey_rgb <<= 8;
					    	grey_rgb |= avg;
					    	
					    }
					    grey_rgb |= OPAQUE;
					    colouredImg.setRGB(j, k, grey_rgb);
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
	
	public void countTiles() {
		logger.info("\tCounting existing ore tiles...");
		long total = 0;
		int mapCount = 0;
		for(BufferedImage img : images) {
			if(img != null) {
				long tileCount = 0;
				for(int i = 0; i < img.getWidth(); ++i) {
					for(int j = 0; j < img.getHeight(); ++j) {
						if((img.getRGB(i, j) & UNSET_ORE_FILTER) != UNSET_ORE_FILTER) {
							++tileCount;
						}
					}
				}
				logger.info("\t\t" + PlanetFace.ALL[mapCount].name + " map existing tile count:" + tileCount);
				mapCount++;
				total += tileCount;
			}
		}
		logger.info("\tTotal existing tiles: " + total);
		
	}
}

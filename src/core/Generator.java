package core;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import config.Ore;
import map.MapData;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;;

public class Generator {
	
	public static final int ORE_EXCLUDER = 0xFFFFFF00;
	public static final int ORE_FILTER = 0x000000FF;
	public static final int ICE_FILTER = 0x00520000; //82 (colour in R channel for ice in hex)
	public static final int SURFACE_HINT_COLOUR = 0xFFA00000; //160 R

	public long generatePatches(MapData mapData, Ore[] ores, float globalPatchSizeMultiplier, float globalPatchSizeVariance, int maxOreTiles, int maxOrePatches, long seed, int surfaceHintColour) {
		List<Pair<Ore, Double>> tempPairedList = new ArrayList<Pair<Ore, Double>>();
		for(Ore ore : ores) {
			tempPairedList.add(new Pair<Ore, Double>(ore, ore.occurenceProbility));
		}
		EnumeratedDistribution<Ore> oreDist = new EnumeratedDistribution<Ore>(tempPairedList);
		
		Random rand = new Random(seed);
		long generatedTiles = 0;
		for(int i = 0; i < maxOrePatches && generatedTiles < maxOreTiles; ++i) {
			generatedTiles += generateOrePatch(mapData, oreDist.sample(), rand, globalPatchSizeMultiplier, globalPatchSizeVariance, surfaceHintColour);
		}
		return generatedTiles;
	}
	
	private int generateOrePatch(MapData mapData, Ore ore, Random rand, float globalPatchSizeMultiplier, float globalPatchSizeVariance, int surfaceHintColour) {
		int tilesAdded = 0;
		int mapSize = mapData.getMapSize();
		int patchSize = Math.round((ore.surfaceArea * globalPatchSizeMultiplier) * (1 + (rand.nextFloat() * 2 - 1) * globalPatchSizeVariance));
		float patchRadius = Math.round(Math.sqrt((double)patchSize) / (ore.density * 2.6));
		float squash = rand.nextFloat() * 1.0f + 0.75f;
		float horizontalSquash;
		float verticalSquash;
		if(rand.nextBoolean()) {
			horizontalSquash = squash;
			verticalSquash = 1 / squash;
		}
		else {
			verticalSquash = squash;
			horizontalSquash = 1 / squash;			
		}
		
		int oreId = ore.centreOreTile == 0 ? ore.id : ore.centreOreTile;
		
		boolean avoidIce = !ore.spawnOnIce;
		float surfaceHint = ore.surfaceHint;
		boolean isSurfaceHint = ore.surfaceHint > 0;
		int mapIndex = rand.nextInt(MapData.MAP_SIDES);
		int startColIndex = rand.nextInt(mapSize);
		int startRowIndex = rand.nextInt(mapSize);
		int colIndex = startColIndex;
		int rowIndex = startRowIndex;
		int centreOreTile = ore.centreOreTile;
		BufferedImage img = mapData.images[mapIndex];
		BufferedImage hintImg = mapData.surfaceHintImages[mapIndex];
		RandomGenerator randGen = new JDKRandomGenerator();
		randGen.setSeed(rand.nextInt());
		int iterations = 0;
		int maxIterations = patchSize * 20;
		do {
			//add patch tiles
			if(rowIndex < mapSize && rowIndex >= 0 && colIndex < mapSize && colIndex >= 0) {
				int pixRGB = img.getRGB(colIndex, rowIndex);
				if((pixRGB | ORE_FILTER) == oreId || (centreOreTile != 0 && (pixRGB | ORE_FILTER) == centreOreTile)) { 
					continue;
				}
				if((avoidIce && ((pixRGB & ICE_FILTER) == ICE_FILTER))) {
					if(iterations == 0) {
						return 0;
					}
					else {
						continue;
					}
				}
				img.setRGB(colIndex, rowIndex, (img.getRGB(colIndex, rowIndex) & ORE_EXCLUDER) | oreId);
				++tilesAdded;
				if(isSurfaceHint && rand.nextFloat() < surfaceHint) {
					hintImg.setRGB(colIndex, rowIndex, surfaceHintColour);
				}
				if(centreOreTile != 0 && iterations == 0) {
					oreId = ore.id;
				}
			}
			colIndex = startColIndex + Math.round((float)(randGen.nextGaussian() * patchRadius) * horizontalSquash);
			rowIndex = startRowIndex + Math.round((float)(randGen.nextGaussian() * patchRadius) * verticalSquash);
		} while(tilesAdded < patchSize && ++iterations < maxIterations);
		return tilesAdded;
	}
	
}

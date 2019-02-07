package core;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import config.Ore;
import map.MapData;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;

import static map.MapData.OPAQUE;
import static map.MapData.ICE_FILTER;
import static map.MapData.ORE_EXCLUDER;
import static map.MapData.ORE_FILTER;

public class Generator {
	
	public long generatePatches(MapData mapData, Ore[] ores, float globalPatchSizeMultiplier, float globalPatchSizeVariance, int maxOreTiles, int maxOrePatches, long seed, int surfaceHintColour, boolean useTestColours) {
		List<Pair<Ore, Double>> tempPairedList = new ArrayList<Pair<Ore, Double>>();
		Map<Ore, Long> oreTileCounts = new HashMap<Ore, Long>();
		for(Ore ore : ores) {
			tempPairedList.add(new Pair<Ore, Double>(ore, ore.occurenceProbility));
			oreTileCounts.put(ore, new Long(0));
		}
		EnumeratedDistribution<Ore> oreDist = new EnumeratedDistribution<Ore>(tempPairedList);
		Random rand = new Random(seed);
		oreDist.reseedRandomGenerator(rand.nextLong());
		long generatedTiles = 0;
		for(int i = 0; i < maxOrePatches && generatedTiles < maxOreTiles; ++i) {
			Ore ore = oreDist.sample();
			int generatedTilesForOre = generateOrePatch(mapData, ore, rand, globalPatchSizeMultiplier, globalPatchSizeVariance, surfaceHintColour, useTestColours);
			generatedTiles += generatedTilesForOre;
			oreTileCounts.put(ore, oreTileCounts.get(ore).longValue() + generatedTilesForOre);
		}

		for(Ore ore : oreTileCounts.keySet()) {
			System.out.println("Tiles generated for ore " + ore.id + " with testColour " + ore.testColour + " is:" + oreTileCounts.get(ore));
		}
		return generatedTiles;
	}
	
	private int generateOrePatch(MapData mapData, Ore ore, Random rand, float globalPatchSizeMultiplier, float globalPatchSizeVariance, int surfaceHintColour, boolean useTestColours) {
		int tilesAdded = 0;
		int mapSize = mapData.getMapSize();
		int mapIndex = rand.nextInt(MapData.MAP_SIDES);
		int startColIndex = rand.nextInt(mapSize);
		int startRowIndex = rand.nextInt(mapSize);
		Random tileRand = new Random(rand.nextLong());
		Random hintRand = new Random(rand.nextLong());
		int patchSize = Math.round((ore.surfaceArea * globalPatchSizeMultiplier) * (1 + (tileRand.nextFloat() * 2 - 1) * globalPatchSizeVariance));
		float patchRadius = Math.round(Math.sqrt((double)patchSize) / (ore.density * 3));
		float squash = tileRand.nextFloat() * 1.0f + 0.75f;
		float horizontalSquash;
		float verticalSquash;
		if(tileRand.nextBoolean()) {
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
		int colIndex = startColIndex;
		int rowIndex = startRowIndex;
		int centreOreTile = ore.centreOreTile;
		BufferedImage img = mapData.images[mapIndex];
		BufferedImage hintImg = mapData.surfaceHintImages[mapIndex];
		BufferedImage colouredImg = mapData.colouredMaps[mapIndex];
		RandomGenerator randGen = new JDKRandomGenerator();
		randGen.setSeed(tileRand.nextInt());
		int iterations = 0;
		int maxIterations = patchSize * 20;
		
		int testColour = 0;
		if(useTestColours) {//get around lack of unsigned ints 
			testColour = Integer.parseInt(ore.testColour, 16) | OPAQUE;
		}
		do {
			//add patch tiles
			//each tile/pixel corresponds to a 30x30m patch of ore in game - measured on EarthLike
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
				if(useTestColours) {
					colouredImg.setRGB(colIndex, rowIndex, testColour);
				}
				if(isSurfaceHint && hintRand.nextFloat() < surfaceHint) {
					hintImg.setRGB(colIndex, rowIndex, surfaceHintColour);
				}
			}
			if(centreOreTile != 0 && iterations == 0) {
				oreId = ore.id;
			}
			colIndex = startColIndex + Math.round((float)(randGen.nextGaussian() * patchRadius) * horizontalSquash);
			rowIndex = startRowIndex + Math.round((float)(randGen.nextGaussian() * patchRadius) * verticalSquash);
		} while(tilesAdded < patchSize && ++iterations < maxIterations);
		return tilesAdded;
	}
	
}

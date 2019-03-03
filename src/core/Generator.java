package core;

import static map.MapData.ICE_FILTER;
import static map.MapData.ORE_EXCLUDER;
import static map.MapData.ORE_FILTER;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import config.OreConfig;
import config.PlanetConfig;
import map.MapData;
public class Generator {
	public static final int[][] GEN_SIDES = {{1,0},{0,1},{-1,0},{0,-1}};
	public static final int[][] GEN_SIDES_ALL = {{1,0},{0,1},{-1,0},{0,-1}, {1,1},{1,-1},{-1,1},{-1,-1}};
	public static final Logger logger = LogManager.getLogger("Generator");
	public static final int RANDOM_SHAPE = 1;
	public static final int[] SHAPES = {2,3,4,5,6};
	Map<String, Map<String, Long>> tileCountMap = new HashMap<String, Map<String, Long>>();
	
	public long generatePatches(MapData mapData, PlanetConfig planetConfig) {
		Map<String, Long> planetTileCountMap = new HashMap<String, Long>();
		List<Pair<OreConfig, Double>> tempPairedList = new ArrayList<Pair<OreConfig, Double>>();
		Map<OreConfig, Long> oreTileCounts = new HashMap<OreConfig, Long>();
		for(OreConfig ore : planetConfig.ores) {
			tempPairedList.add(new Pair<OreConfig, Double>(ore, ore.p));
			oreTileCounts.put(ore, new Long(0));
		}
		EnumeratedDistribution<OreConfig> oreDist = new EnumeratedDistribution<OreConfig>(tempPairedList);
		Random rand = new Random(planetConfig.seed);
		oreDist.reseedRandomGenerator(rand.nextLong());
		long generatedTiles = 0;
		for(int i = 0; i < planetConfig.maxOrePatches && generatedTiles < planetConfig.maxOreTiles; ++i) {
			OreConfig ore = oreDist.sample();
			int generatedTilesForOre = generateOrePatch(mapData, ore, rand);
			if(planetTileCountMap.containsKey(ore.getOreName())) {
				planetTileCountMap.put(ore.getOreName(), planetTileCountMap.get(ore.getOreName()).longValue() + generatedTilesForOre);
			}
			else {
				planetTileCountMap.put(ore.getOreName(), new Long(generatedTilesForOre));
			}
			generatedTiles += generatedTilesForOre;
			oreTileCounts.put(ore, oreTileCounts.get(ore).longValue() + generatedTilesForOre);
		}

		for(OreConfig ore : oreTileCounts.keySet()) {
			logger.info("\t\tTiles generated for ore " + ore.type + " (id:" + ore.id + ") with testColour " + Integer.toHexString(ore.testColour) + " : " + oreTileCounts.get(ore));
		}
		tileCountMap.put(planetConfig.name, planetTileCountMap);
		return generatedTiles;
	}
	
	private int generateOrePatch(MapData mapData, OreConfig ore, Random rand) {
		// The ugly bit
		int tilesAdded = 0;
		int mapSize = mapData.getMapSize();
		int mapIndex = ore.planetFaces[rand.nextInt(ore.planetFaces.length)].index;
		int startColIndex = rand.nextInt(mapSize);
		int startRowIndex = rand.nextInt(mapSize);
		Random tileRand = new Random(rand.nextLong());
		
		Random hintRand = new Random(rand.nextLong());
		int oreShape = ore.shape;
		if(oreShape == RANDOM_SHAPE) {
			oreShape = SHAPES[tileRand.nextInt(SHAPES.length)];
		}
		int patchSize = Math.round((ore.surfaceArea * ore.surfaceAreaMultiplier) * (1 + (tileRand.nextFloat() * 2 - 1) * ore.surfaceAreaVariance));
		float patchDiameter = Math.round(Math.sqrt((double)patchSize) / (ore.density));
		float patchRadius = patchDiameter / 2f;
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
		int width = Math.round(patchDiameter * horizontalSquash);
		int height = Math.round(patchDiameter * verticalSquash);
		int oreId = ore.centreOreTile >= 0 ? ore.centreOreTile : ore.id;
		
		boolean avoidIce = ore.avoidIce;
		boolean isSurfaceHint = ore.surfaceHintMaps && ore.surfaceHintProbability > 0;
		int colIndex = startColIndex;
		int rowIndex = startRowIndex;
		int centreOreTile = ore.centreOreTile;
		BufferedImage img = mapData.images[mapIndex];
		BufferedImage hintImg = mapData.surfaceHintImages[mapIndex];
		BufferedImage colouredImg = mapData.colouredMaps[mapIndex];
		int iterations = 0;
		int maxIterations = patchSize * 30;
		//for line shapes
		int targetCol = startColIndex;
		int targetRow = startRowIndex;
		double linearCoefficient = 0;
		double linearXDist = 0;
		double linearXIncrement = 0;
		int sourceColIndex = 0;
		int sourceRowIndex = 0;
		double colDiff = 0;
		double rowDiff = 0;
		boolean colMet, rowMet;
		int lastColIndex = startColIndex;
		int lastRowIndex = startRowIndex;
		int halfWidth = width/2;
		int halfHeight = height/2;
		int x = 0 - halfWidth;
		int y = 0 - halfHeight;
		int layer = 1;
		int foundCount = 0;
		boolean found;
		do {
			boolean paintTile = true;
			// add patch tiles
			// each tile/pixel corresponds to a 30x30m patch of ore in game - measured on EarthLike
			if(rowIndex >= mapSize || rowIndex < 0 || colIndex >= mapSize || colIndex < 0) {
				paintTile = false;
			}
			if(paintTile) {
				int pixRGB = img.getRGB(colIndex, rowIndex);
				if((pixRGB | ORE_FILTER) == oreId || (centreOreTile != -1 && (pixRGB | ORE_FILTER) == centreOreTile)) { 
					paintTile = false;
				}
				if((avoidIce && ((pixRGB & ICE_FILTER) == ICE_FILTER))) {
					if(iterations == 0) {
						//If starting on an ice lake, abort
						return 0;
					}
					else {
						paintTile = false;
					}
				}
			}
			if(paintTile) {
				img.setRGB(colIndex, rowIndex, (img.getRGB(colIndex, rowIndex) & ORE_EXCLUDER) | oreId);
				++tilesAdded;
				if(ore.makeColouredMaps) {
					colouredImg.setRGB(colIndex, rowIndex, ore.testColour);
				}
				if(isSurfaceHint && hintRand.nextFloat() < ore.surfaceHintProbability) {
					hintImg.setRGB(colIndex, rowIndex, ore.surfaceHintColour);
				}
			}
			if(centreOreTile != -1 && iterations == 0) {
				oreId = ore.id;
			}
			// Handle different shapes
			switch(oreShape) {
			case 7:
				// diamonds
				found = false;
				while(!found) {
					for(int i = 0; i < GEN_SIDES_ALL.length && !found;++i) {
						int colInc = GEN_SIDES_ALL[i][0];
						int rowInc = GEN_SIDES_ALL[i][1];
						colDiff = Math.abs(startColIndex - (colIndex + colInc));
						rowDiff = Math.abs(startRowIndex - (rowIndex + rowInc));
						if(colDiff + rowDiff == layer && !(lastColIndex == colIndex + colInc && lastRowIndex == rowIndex + rowInc)) {

							lastColIndex = colIndex;
							lastRowIndex = rowIndex;
							//paint it
							colIndex += colInc;
							rowIndex += rowInc;
							
							++foundCount;
							double weightedColDiff = colDiff * horizontalSquash;
							double weightedRowDiff = rowDiff * verticalSquash;
							double crowSquared = weightedColDiff * weightedColDiff + weightedRowDiff * weightedRowDiff; 
							if(Math.min(crowSquared, patchRadius * patchRadius * 2/3) < (tileRand.nextDouble() * patchRadius * patchRadius)) {
								found = true;
							}
						}
					}
					if((foundCount >= (4 * layer))) {
						foundCount = foundCount % (4 * layer);
						++layer;
					}
				}
			break;
			case 6:
				// sparse circles
				found = false;
				while(!found) {
					while((x <= halfWidth) && !found) {
						while((y <= halfHeight) && !found) {
							colDiff = startColIndex - x;
							rowDiff = startRowIndex - y;
							double crowDist = Math.sqrt((x * x) + (y * y)); 
							if(Math.abs(crowDist - (double)layer) <= 1.0d) {
								if(tileRand.nextDouble() /*- (1f / (float)Math.min(layer, patchRadius))*/ <= ore.density) {
									colIndex = startColIndex + x;
									rowIndex = startRowIndex + y;
									found = true;
								}
							}
							++y;
						}
						if(y > halfHeight) {
							y = 0 - halfHeight;
							++x;
						}
					}
//					for(; y<=halfHeight && !found; y++) {
//					    for(; x<=halfWidth && !found; x++) {
//					        if(x*x*halfHeight*halfHeight+y*y*halfWidth*halfWidth <= halfHeight*halfHeight*halfWidth*halfWidth) {
//					            colIndex = startColIndex + x;
//					        	rowIndex = startRowIndex + y;
//					        	found = true;
//					        }
//					    }
//					}
					if(!found) {
						++layer;
						x = 0 - halfWidth;
						y = 0 - halfWidth;
						break;
					}
				}
			break;
			case 5:
				// fuzzy gaussian line
				colMet = (colIndex >= targetCol && sourceColIndex <= targetCol) || (colIndex <= targetCol && sourceColIndex >= targetCol);
				rowMet = (rowIndex >= targetRow && sourceRowIndex <= targetRow) || (rowIndex <= targetRow && sourceRowIndex >= targetRow);
				if(colMet && rowMet) {
					//new target
					targetCol = startColIndex + Math.round((float)(tileRand.nextFloat() - 0.5d) * width);
					targetRow = startRowIndex + Math.round((float)(tileRand.nextFloat() - 0.5d) * height);
					sourceColIndex = colIndex;
					sourceRowIndex = rowIndex;
					colDiff = targetCol - colIndex;
					rowDiff = targetRow - rowIndex;
					//dodge x/0
					linearCoefficient = rowDiff / (colDiff == 0d ? colDiff + 0.4d : colDiff);
					linearXIncrement = (colDiff / Math.abs(rowDiff)) * 0.2d;
					linearXDist = 0;
				}
				colIndex = (int)Math.round(linearXDist + (double)tileRand.nextInt(3)-1) + sourceColIndex;
				rowIndex = (int)Math.round((linearXDist*linearCoefficient) + (double)tileRand.nextInt(3)-1) + sourceRowIndex;
				linearXDist += linearXIncrement;

			break;
			case 4:
				// step Gaussian lines
				colMet = (colIndex >= targetCol && sourceColIndex <= targetCol) || (colIndex <= targetCol && sourceColIndex >= targetCol);
				rowMet = (rowIndex >= targetRow && sourceRowIndex <= targetRow) || (rowIndex <= targetRow && sourceRowIndex >= targetRow);
				
				if(colMet && rowMet) {
					// new target
					targetCol = startColIndex + Math.round((float)(tileRand.nextGaussian() - 0.5d) * width / 3);
					targetRow = startRowIndex + Math.round((float)(tileRand.nextGaussian() - 0.5d) * height / 3);
					sourceColIndex = colIndex;
					sourceRowIndex = rowIndex;
				}
				colDiff = targetCol - colIndex;
				rowDiff = targetRow - rowIndex;
				int colSign = colDiff >= 0 ? 1 : -1;
				int rowSign = rowDiff >= 0 ? 1 : -1;
				colDiff = Math.abs(colDiff);
				rowDiff = Math.abs(rowDiff);
				if(colDiff > rowDiff) {
					colIndex += colSign;
				}
				else if(colDiff < rowDiff) {
					rowIndex += rowSign;
				}
				else {
					if(tileRand.nextBoolean()) {
						colIndex += colSign;
					}
					else {
						rowIndex += rowSign;
					}
				}
			break;
			case 3:
				// snek
				int[] side = GEN_SIDES[tileRand.nextInt(GEN_SIDES.length)];
				
				if(colIndex > (startColIndex + width/2) || colIndex < (startColIndex - width/2) || 
						rowIndex > (startRowIndex + height/2) || rowIndex < (startRowIndex - height/2)) {
					colIndex = startColIndex + side[0];
					rowIndex = startRowIndex + side[1];
				}
				else {
					colIndex = colIndex + side[0];
					rowIndex = rowIndex + side[1];
				}
			break;
			default:
			case 2:
				// gaussian
				colIndex = startColIndex + Math.round((float)((tileRand.nextGaussian() - 0.5d) * patchDiameter *(1f/3f)) * horizontalSquash);
				rowIndex = startRowIndex + Math.round((float)((tileRand.nextGaussian() - 0.5d) * patchDiameter * (1f/3f)) * verticalSquash);
			break;
				
			}
		} while(tilesAdded < patchSize && ++iterations < maxIterations);
		return tilesAdded;
	}
	
	
}

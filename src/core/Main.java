package core;

import java.awt.image.BufferedImage;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import config.Config;
import config.Ore;
import map.MapData;
import map.MapHandler;

public class Main {
	List<Config> configs;
	MapHandler mapHandler;
	MapData mapData;
	Generator generator;
	
	public static final int UNSET_ORE_FILTER = 0x000000FF;

	public static void main(String[] args) {
		new Main().run();
	}
	
	Main() {
//		generateTestConfig();
		String configFile = "config.json";
		System.out.println("Attempting to load " + configFile);
		configs = Config.loadConfigs(configFile);
		mapHandler = new MapHandler();
		mapData = new MapData();
		generator = new Generator();
	}
	
	public void run() {
		System.out.println("Starting ore generation");
		for(Config config : configs) {
			System.out.println("Loading map data from: " + config.getPlanetDataPath());
			mapData = mapHandler.loadMapData(config.getPlanetDataPath(), config.isLoadSurfaceHintMaps(), config.isMakeColouredMaps());
			countTiles(mapData);
			config.cascadeOverrides();
			System.out.println("Clearing existing ore data");
			mapData.clearOreData();
			System.out.println("Generating ore tiles...");
			long tilesGenerated = generator.generatePatches(mapData, config.getOres(), config.getGlobalPatchSizeMultiplier(), 
					config.getGlobalPatchSizeVariance(), config.getMaxOreTiles(), config.getMaxOrePatches(), 
					config.getSeed(), config.getSurfaceHintColour(), config.isMakeColouredMaps());
			System.out.println("Tiles generated:" + tilesGenerated);
			System.out.println("Writing ore data to map images in: " + config.getPlanetDataOutputPath());
			mapHandler.writeMapData(mapData, config.getPlanetDataOutputPath(), config.isLoadSurfaceHintMaps(), config.isMakeColouredMaps());
			System.out.println("Map images saved");
		}
		System.out.println("Done.");
	}
	
	public void generateTestConfig() {
		Ore[] ores = {new Ore(10, 0.2, 100, 1.0f, 1, 1.0f, false, 0, "0xFFFFFF"),
					  new Ore(10, 0.2, 100, 1.0f, 1, 1.0f, false, 0, "0xFFF000")};
		configs = new ArrayList<Config>();
		configs.add(new Config("./", "./", 1.0f, 0.4f, ores, 10000, 1000, 7));
		configs.add(new Config("./", "./", 1.0f, 0.4f, ores, 10000, 1000, 7));		
		
	}
	
	String getConfigJSON() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(configs, new TypeToken<ArrayList<Config>>(){}.getType());
	}
	
	void countTiles(MapData mapData) {
		for(BufferedImage img : mapData.images) {
			if(img != null) {
				long tileCount = 0;
				for(int i = 0; i < img.getWidth(); ++i) {
					for(int j = 0; j < img.getHeight(); ++j) {
						if((img.getRGB(i, j) & UNSET_ORE_FILTER) != UNSET_ORE_FILTER) {
							++tileCount;
						}
					}
				}
				System.out.println("old tile count:" + tileCount);
			}
		}
	}
}

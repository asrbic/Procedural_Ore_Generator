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
		configs = Config.loadConfigs("config.json");
		mapHandler = new MapHandler();
		mapData = new MapData();
		generator = new Generator();
	}
	
	public void run() {
		for(Config config : configs) {
			mapData = mapHandler.loadMapData(config.getPlanetDataPath(), config.isLoadSurfaceHintMaps());
			if(config.isCountCurrent()) {
				countTiles(mapData);
			}
			mapData.clearOreData();
			long tilesGenerated = generator.generatePatches(mapData, config.getOres(), config.getGlobalPatchSizeMultiplier(), config.getGlobalPatchSizeVariance(), config.getMaxOreTiles(), config.getMaxOrePatches(), config.getSeed());
			System.out.println("Tiles generated:" + tilesGenerated);
			mapHandler.writeMapData(mapData, config.getPlanetDataOutputPath(), config.isLoadSurfaceHintMaps());
		}
	}
	
	public void generateTestConfig() {
		Ore[] ores = {new Ore(10, 0.2, 100, 1.0f, 1, 1.0f, false),
					  new Ore(10, 0.2, 100, 1.0f, 1, 1.0f, false)};
		configs = new ArrayList<Config>();
		configs.add(new Config("./", "./", 1.0f, 0.4f, ores, 10000, 1000, 7, true));
		configs.add(new Config("./", "./", 1.0f, 0.4f, ores, 10000, 1000, 7, true));		
		
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

package core;

import java.io.File;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import config.GlobalConfig;
import config.PlanetConfig;
import map.MapData;
import map.MapHandler;

public class Main {
	GlobalConfig config;
	MapData mapData;
	Generator generator;
	Thread[] imageWriterThreads;

	public static void main(String[] args) {
		new Main().run();
	}
	
	Main() {
//		generateTestConfig();
		mapData = new MapData();
		generator = new Generator();
	}
	
	public void run() {
		String configFile = "config.json";
		System.out.println("Attempting to load " + configFile);
		config = GlobalConfig.loadConfig(configFile);
		config.cascadeSettings();
		if(config.planetMaterialsFilePath != null) {
			updatePlanetGeneratorDefinitions();
		}
		imageWriterThreads = new Thread[config.planets.length];
		generate();
		System.out.println("Waiting for all image compression/writer threads to finish...");
	}
	
	public void updatePlanetGeneratorDefinitions() {
		try {
			System.out.println("Attempting to update PlanetGeneratorDefinitions file..");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = dbf.newDocumentBuilder();
			File f = new File(config.planetMaterialsFilePath);
			Document doc = builder.parse(f);
			System.out.println("Updated PlanetGeneratorDefinitions file");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void generate() {
		int threadId = 0;
		for(PlanetConfig planetConfig : config.planets) {
			MapData mapData = new MapData();
			MapHandler handler = new MapHandler(mapData, Paths.get(config.planetDataPath, planetConfig.name).toString(), 
					Paths.get(config.planetDataOutputPath, planetConfig.name).toString(), planetConfig.surfaceHintMaps, planetConfig.makeColouredMaps);
			System.out.println("Processing planet \"" + planetConfig.name + "\"");
			System.out.println("\tLoading map data from: " + Paths.get(config.planetDataPath, planetConfig.name).toString());
			handler.loadMapData();
			if(config.countExistingTiles) {
				mapData.countTiles();
			}
			System.out.println("\tClearing existing ore data");
			mapData.clearOreData();
			System.out.println("\tGenerating ore tiles with effective ore configs:\n" + toJSON(planetConfig.ores));
			long tilesGenerated = generator.generatePatches(mapData, planetConfig);
			System.out.println("\tTiles generated:" + tilesGenerated);
			System.out.println("\tWriting ore data to map images in: " + Paths.get(config.planetDataOutputPath, planetConfig.name).toString());
			imageWriterThreads[threadId] = new Thread(handler);
			imageWriterThreads[threadId].start();
			++threadId;
		}
	}
	
	public void generateTestConfig() {
//		OreConfig[] ores = {new OreConfig(10, 0.2, 100, 1.0f, 1, 1.0f, false, 0, "0xFFFFFF"),
//					  new OreConfig(10, 0.2, 100, 1.0f, 1, 1.0f, false, 0, "0xFFF000")};
//		configs = new ArrayList<GlobalConfig>();	
		
	}
	
	String toJSON(Object o) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(o);
	}
	

}

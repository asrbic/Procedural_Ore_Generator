package core;


import java.io.File;
import java.io.FilenameFilter;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import config.GlobalConfig;
import config.PlanetConfig;
import map.MapData;
import map.MapHandler;
import xml.XMLConfigUpdater;

public class Main {
	public static final Logger logger = LogManager.getLogger("Main");

	GlobalConfig config;
	MapData mapData;
	Generator generator;
	XMLConfigUpdater xmlUpdater;
	Thread[] imageWriterThreads = null;
	
	public static void main(String[] args) {
		new Main().run();
	}
	
	public Main() {
		mapData = new MapData();
		generator = new Generator();
		xmlUpdater = new XMLConfigUpdater();
	}
	
	public void run() {
		String configFile = "config.json";
		logger.info("Attempting to load " + configFile);
		config = GlobalConfig.loadConfig(configFile);
		if(config == null) {
			return;
		}
		GlobalConfig defaultConfig = new GlobalConfig();
		defaultConfig.setDefaults();
		config.copyDefaults(defaultConfig);
		config.cascadeSettings();
		if(config.planetGeneratorDefinitionsPath != null) {
			xmlUpdater.updatePlanetGeneratorDefinitions(config);
		}
		if(!config.makeColouredMaps) {
			deleteColouredTestFiles();
		}
		if(config.planetDataPath != null) {
			imageWriterThreads = new Thread[config.planets.length];
			generate();
			logger.info("Steam workshop table summary:\n" + getSteamWorkshopSummary());
		}
		if(imageWriterThreads != null) {
			logger.info("Waiting for all image compression/writer threads to finish...");
			int i = 0;
			for(Thread t : imageWriterThreads) {
				try{
					t.join();
					logger.info("Images for " + config.planets[i].name + " done");
				}
				catch(InterruptedException e) {
					logger.error(e);
				}
				++i;
			}
			logger.info("All Image compression/writer threads complete");
		}
		logger.info("Done. Press the ENTER key to exit");
		Scanner exit = new Scanner(System.in);
		exit.nextLine();
		exit.close();
	}
	

	
	public void generate() {
		int threadId = 0;
		for(PlanetConfig planetConfig : config.planets) {
			MapData mapData = new MapData();
			MapHandler handler = new MapHandler(mapData, Paths.get(config.planetDataPath, planetConfig.name).toString(), 
					Paths.get(config.planetDataOutputPath, planetConfig.name).toString(), planetConfig.surfaceHintMaps, planetConfig.makeColouredMaps);
			logger.info("Processing planet \"" + planetConfig.name + "\"");
			logger.info("\tLoading map data from: " + Paths.get(config.planetDataPath, planetConfig.name).toString());
			handler.loadMapData();
			if(config.countExistingTiles) {
				mapData.countTiles();
			}
			logger.info("\tClearing existing ore data");
			mapData.clearOreData();
			logger.info("\tGenerating ore tiles with effective ore configs:\n" + toJSON(planetConfig.ores));
			long tilesGenerated = generator.generatePatches(mapData, planetConfig);
			logger.info("\tTiles generated:" + tilesGenerated);
			logger.info("\tWriting ore data to map images in: " + Paths.get(config.planetDataOutputPath, planetConfig.name).toString());
			imageWriterThreads[threadId] = new Thread(handler);
			imageWriterThreads[threadId].start();
			++threadId;
		}
	}
	
	public String getSteamWorkshopSummary() {
		List<String> uniqueOres = new ArrayList<String>();
		for(String planetName : generator.tileCountMap.keySet()) {
			for(String oreName : generator.tileCountMap.get(planetName).keySet()) {
				if(!uniqueOres.contains(oreName)) {
					uniqueOres.add(oreName);
				}
			}
		}
		StringWriter w = new StringWriter();
		w.append("[table]\n")
		.append("[tr]\n")
		.append("[th][/th]\n");
		List<String> planetNames = new ArrayList<String>();
		for(PlanetConfig planet : config.planets) {
			if(generator.tileCountMap.containsKey(planet.name)) {
				w.append("[th]" + planet.name + "[/th]\n");
				planetNames.add(planet.name);
			}
		}
		w.append("[/tr]\n");
		for(String oreName : uniqueOres) {

			w.append("[tr][th]" + oreName + "[/th]\n");
			for(String planetName : planetNames) {
				Long oreCount = generator.tileCountMap.get(planetName).get(oreName);
				if(oreCount == null) {
					oreCount = 0l;
				}
				w.append("[td]" + oreCount + "[/td]\n");
			}
			w.append("[/tr]\n");
		}
		w.append("[/table]\n");
		return w.toString();
	}
	
	String toJSON(Object o) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(o);
	}
	
	private void deleteColouredTestFiles() {
		for(PlanetConfig planet : config.planets) {
			File planetDir = Paths.get(config.planetDataOutputPath, planet.name).toFile();
			if(planetDir.exists()) {
				File[] images = planetDir.listFiles((File dir, String name) -> {
					return name.contains("coloured");
				});
				for(File image : images) {
					image.delete();
				}
			}
		}
	}
	

}

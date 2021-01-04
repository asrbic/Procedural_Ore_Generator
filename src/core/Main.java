package core;


import java.io.File;
import java.io.FilenameFilter;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

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
	ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
	Set<Future<Boolean>> generatefutures = new HashSet<>();
	Map<String, Future> imagefutures = new ConcurrentHashMap<>();

	public static void main(String[] args) throws Exception {
		try {
			new Main().run();
		}
		catch(Exception e) {
			logger.error("Failed, exception occurred: ", e);
			throw e;
		}
		finally
		{
			logger.info("Done. Press the ENTER key to exit");
			Scanner exit = new Scanner(System.in);
			exit.nextLine();
			exit.close();
			logger.info("Done.");
		}
	}
	
	public Main() {
		mapData = new MapData();
		generator =  new Generator();
		xmlUpdater = new XMLConfigUpdater();
	}
	
	public void run() throws Exception {
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
		if(config.planetGeneratorDefinitionsPath != null || !config.planetGeneratorDefinitionsPathArray.isEmpty()) {
			if(!xmlUpdater.updatePlanetGeneratorDefinitions(config)) {
				return;
			}
		}
		if(!config.makeColouredMaps) {
			deleteColouredTestFiles();
		}
		if(config.planetDataPath != null) {
			if(!generate()) {
				return;
			}
			logger.info("Steam workshop table summary:\n" + getSteamWorkshopSummary());
		}
			logger.info("Waiting for all image compression/writer threads to finish...");
		for (Map.Entry<String, Future> imageWriterFuture : imagefutures.entrySet()) {
			imageWriterFuture.getValue().get();
		}
		executor.shutdown();
		singleThreadExecutor.shutdown();
		logger.info("All Image compression/writer threads complete");
	}
	

	
	public boolean generate() throws ExecutionException, InterruptedException {
		for(final PlanetConfig planetConfig : config.planets) {
			generatefutures.add(executor.submit(() -> {
				MapData mapData = new MapData();
				MapHandler handler = new MapHandler(planetConfig.name, mapData, Paths.get(config.planetDataPath, planetConfig.name).toString(),
						Paths.get(config.planetDataOutputPath, planetConfig.name).toString(), planetConfig.surfaceHintMaps, planetConfig.makeColouredMaps);
				logger.info(planetConfig.name + ": Processing planet \"" + planetConfig.name + "\"");
				logger.info(planetConfig.name + ":\tLoading map data from: " + Paths.get(config.planetDataPath, planetConfig.name).toString());
				if(handler.loadMapData() == null) {
					return false;
				}

				if(config.countExistingTiles) {
					mapData.countTiles();
				}
				logger.info(planetConfig.name + ":\tClearing existing ore data");
				mapData.clearOreData();
				logger.info(planetConfig.name + ":\tGenerating ore tiles with effective ore configs:\n" + toJSON(planetConfig.ores));
				long tilesGenerated = generator.generatePatches(mapData, planetConfig);
				logger.info(planetConfig.name + ":\tTiles generated:" + tilesGenerated);
				logger.info(planetConfig.name + ":\tWriting ore data to map images in: " + Paths.get(config.planetDataOutputPath, planetConfig.name).toString());
				if(config.concurrentImageWrite)
					imagefutures.put(planetConfig.name, executor.submit(handler));
				else
					imagefutures.put(planetConfig.name, singleThreadExecutor.submit(handler));
				return true;
			}));

		}
		for (Future<Boolean> generatefuture : generatefutures) {
			if(false == generatefuture.get()) {return false;}
		}
		return true;
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

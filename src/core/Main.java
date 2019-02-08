package core;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import config.GlobalConfig;
import config.OreConfig;
import config.PlanetConfig;
import map.MapData;
import map.MapHandler;
import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;

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
			Map<String, PlanetConfig> planetLookup = new HashMap<String, PlanetConfig>();
			for(PlanetConfig pc : config.planets) {
				planetLookup.put(pc.name, pc);
			}
			File f = new File(config.planetMaterialsFilePath);
			Builder parser = new Builder(false);
			Document doc = parser.build(f);
			Nodes definitions = doc.query("Definitions/Definition");
			for(int i = 0; i < definitions.size();++i) {
				Node def = definitions.get(i);
				String subTypeId = def.query("Id/SubtypeId").get(0).getValue();
				if(planetLookup.containsKey(subTypeId)) {
					PlanetConfig planet = planetLookup.get(subTypeId);
					Node oreMappings = def.query("OreMappings").get(0);
					Element e = (Element)oreMappings;
					e.removeChildren();
					for(int j = 0; j < planet.ores.length; ++j) {
						OreConfig ore = planet.ores[j];
						Element oreElement = new Element("Ore");
						oreElement.addAttribute(new Attribute("Value", Integer.toString(ore.id)));
						oreElement.addAttribute(new Attribute("Type", ore.type));
						oreElement.addAttribute(new Attribute("Start", Integer.toString(ore.startDepth)));
						oreElement.addAttribute(new Attribute("Depth", Integer.toString(ore.depth)));
						oreElement.addAttribute(new Attribute("TargetColor", ore.mappingFileTargetColour));
						oreElement.addAttribute(new Attribute("ColorInfluence", ore.mappingFileTargetColour));
						e.appendChild(oreElement);
					}
				}
			}
			PrintWriter writer = new PrintWriter(f);
			writer.print(doc.toXML());
			writer.close();
//			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//			DocumentBuilder builder = dbf.newDocumentBuilder();
//			
//			Document doc = builder.parse(f);

//			NodeList definitions = doc.getElementsByTagName("Definition");
//			for(int i = 0; i < definitions.getLength(); ++i) {
//				Node definition = definitions.item(i);
//				String subTypeId = definition.
//			}
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

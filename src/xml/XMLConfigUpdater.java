package xml;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import config.GlobalConfig;
import config.OreConfig;
import config.PlanetConfig;
import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class XMLConfigUpdater {
	public static final Logger logger = LogManager.getLogger("XMLConfigUpdater");

	public boolean updatePlanetGeneratorDefinitions(GlobalConfig config) {
		try {
			logger.info("Attempting to update PlanetGeneratorDefinitions file..");
			Map<String, PlanetConfig> planetLookup = new HashMap<String, PlanetConfig>();
			for (PlanetConfig pc : config.planets) {
				planetLookup.put(pc.name, pc);
			}
			File f = new File(config.planetGeneratorDefinitionsPath);
			if (!f.exists()) {
				JOptionPane.showMessageDialog(null,
						"Unable to find file: " + config.planetGeneratorDefinitionsPath, "Cannot find planet generator definitions file",
						JOptionPane.ERROR_MESSAGE);
				logger.error("Unable to find file: " + config.planetGeneratorDefinitionsPath);
				return false;
			}
			Builder parser = new Builder(false);
			Document doc = parser.build(f);
			Nodes definitions = doc.query("Definitions/Definition");
			for (int i = 0; i < definitions.size(); ++i) {
				Node def = definitions.get(i);
				String subTypeId = def.query("Id/SubtypeId").get(0).getValue();
				if (planetLookup.containsKey(subTypeId)) {
					PlanetConfig planet = planetLookup.get(subTypeId);
					Node oreMappings = def.query("OreMappings").get(0);
					Element e = (Element) oreMappings;
					e.removeChildren();
					for (int j = 0; j < planet.ores.length; ++j) {
						OreConfig ore = planet.ores[j];
						Element oreElement = new Element("Ore");
						oreElement.addAttribute(new Attribute("Value", Integer.toString(ore.id)));
						oreElement.addAttribute(new Attribute("Type", ore.type));
						oreElement.addAttribute(new Attribute("Start", Integer.toString(ore.startDepth)));
						oreElement.addAttribute(new Attribute("Depth", Integer.toString(ore.depth)));
						oreElement.addAttribute(new Attribute("TargetColor", ore.mappingFileTargetColour));
						oreElement.addAttribute(
								new Attribute("ColorInfluence", Integer.toString(ore.mappingFileColourInfluence)));
						e.appendChild(oreElement);
					}
				}
			}
			File out = Paths.get(config.planetGeneratorDefinitionsOutputPath + "PlanetGeneratorDefinitions.sbc").toFile();
			PrintWriter writer = new PrintWriter(out);
			writer.print(doc.toXML());
			writer.close();
			logger.info("Updated PlanetGeneratorDefinitions file");
			return true;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,
					"Unable to parse " + config.planetGeneratorDefinitionsPath + "\n" + e.toString(), "XML parse error",
					JOptionPane.ERROR_MESSAGE);
			logger.error("Unable to parse " + config.planetGeneratorDefinitionsPath + "\n" + e.toString());
			return false;
		}
	}
}

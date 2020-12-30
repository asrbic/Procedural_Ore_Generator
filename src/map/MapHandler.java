package map;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import config.CommonConfig.PlanetFace;

public class MapHandler implements Runnable {
	public static final Logger logger = LogManager.getLogger("MapHandler");

public static final String MAT = "_mat.png";
public static final String ADD = "_add.png";
public static final String COLOURED = "_coloured.png";

	String planetName;
	MapData mapData;
	String inputPath;
	String outputPath;
	boolean surfaceHintMaps;
	boolean colouredMaps;
	
	public MapHandler(String planetName, MapData mapData, String inputPath, String outputPath, boolean surfaceHintMaps, boolean colouredMaps) {
		this.planetName = planetName;
		this.mapData = mapData;
		this.inputPath = inputPath;
		this.outputPath = outputPath;
		this.surfaceHintMaps = surfaceHintMaps;
		this.colouredMaps = colouredMaps;
	}
	
	public MapData loadMapData() {
		for(int i = 0; i < PlanetFace.ALL.length; ++i) {
			String mapName = PlanetFace.ALL[i].name;
			try {
				mapData.images[i] = ImageIO.read(Paths.get(inputPath, mapName + MAT).toFile());
				if(surfaceHintMaps) {
					Path imagePath = Paths.get(inputPath, mapName + ADD);
					File f = imagePath.toFile();
					if(f.exists())
						mapData.surfaceHintImages[i] = ImageIO.read(f);
					else
						mapData.surfaceHintImages[i] = null;
				}
				if(colouredMaps) {
					mapData.colouredMaps[i] = ImageIO.read(Paths.get(inputPath, mapName + MAT).toFile());
				}
			}
			catch(Exception e) {
				logger.error("Exception occurred while loading map data (images):", e);
				JOptionPane.showMessageDialog(null, "Unable to load image files from directory:\n" + inputPath, "File Read Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}
		}
		mapData.calculateMapSize();

		return mapData;
	}
	
	public void writeMapData() {
		try {
			
			for(int i = 0; i < mapData.images.length; ++i) {
				if(mapData.images[i] == null) {
					continue;
				}
				String mapName = PlanetFace.ALL[i].name;
				Path path = Paths.get(outputPath);
				if(Files.notExists(path)) {
					Files.createDirectories(path);
				}
				ImageIO.write(mapData.images[i], "png" , Paths.get(outputPath, mapName + MAT).toFile());
				if(surfaceHintMaps) {
					if(mapData.surfaceHintImages[i] != null)
						ImageIO.write(mapData.surfaceHintImages[i], "png" , Paths.get(outputPath, mapName + ADD).toFile());
				}
				if(colouredMaps) {
					ImageIO.write(mapData.colouredMaps[i], "png" , Paths.get(outputPath, mapName + COLOURED).toFile());
				}
			}
		}
		catch(Exception e) {
			logger.error("Exception occurred while writing map data (images): ", e);
			JOptionPane.showMessageDialog(null, "Unable to create image files in directory:\n" + outputPath, "File Write Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		logger.info("Images for " + planetName + " done.");
	}

	@Override
	public void run() {
		//write
		writeMapData();
	}
}

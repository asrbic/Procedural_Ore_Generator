package map;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class MapHandler {

public static final String MAT = "_mat.png";
public static final String ADD = "_add.png";
public static final String FRONT_MAP_NAME = "front";
public static final String LEFT_MAP_NAME = "left";
public static final String RIGHT_MAP_NAME = "right";
public static final String UP_MAP_NAME = "up";
public static final String DOWN_MAP_NAME = "down";
public static final String BACK_MAP_NAME = "back";


public static final String[] MAP_NAMES = {FRONT_MAP_NAME, LEFT_MAP_NAME, RIGHT_MAP_NAME, UP_MAP_NAME, DOWN_MAP_NAME, BACK_MAP_NAME};

	public MapData loadMapData(String path, boolean loadSurfaceHintMaps) {
		MapData mapData = new MapData();
		for(int i = 0; i < 1/*MAP_NAMES.length*/; ++i) {
			String mapName = MAP_NAMES[i];
			try {
				mapData.images[i] = ImageIO.read(Paths.get(path, mapName + MAT).toFile());
				if(loadSurfaceHintMaps) {
					mapData.surfaceHintImages[i] = ImageIO.read(Paths.get(path, mapName + ADD).toFile());
				}
			}
			catch(Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Unable to load image files from directory:\n" + path, "File Read Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}
		}
		mapData.calculateMapSize();

		return mapData;
	}
	
	public void writeMapData(MapData mapData, String outputPath, boolean loadSurfaceHintMaps) {
		try {
			
			for(int i = 0; i < mapData.images.length; ++i) {
				if(mapData.images[i] == null) {
					continue;
				}
				String mapName = MAP_NAMES[i];
				Path path = Paths.get(outputPath);
				if(Files.notExists(path)) {
					Files.createDirectories(path);
				}
				ImageIO.write(mapData.images[i], "png" , Paths.get(outputPath, mapName + MAT).toFile());
				if(loadSurfaceHintMaps) {
					ImageIO.write(mapData.surfaceHintImages[i], "png" , Paths.get(outputPath, mapName + ADD).toFile());
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Unable to create image files in directory:\n" + outputPath, "File Write Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}
}

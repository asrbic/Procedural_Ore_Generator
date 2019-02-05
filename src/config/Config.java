package config;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class Config {

	float globalPatchSizeMultiplier;
	float globalPatchSizeVariance;
	int maxOreTiles;
	int maxOrePatches;
	long seed;
	String planetDataPath;
	String planetDataOutputPath;
	boolean loadSurfaceHintMaps;
	boolean countCurrent;
	Ore[] ores;
	
	public static Config loadConfig(String path) {
		Gson gson = new Gson();
		byte[] encoded;
		try {
			encoded = Files.readAllBytes(Paths.get(path));
			return gson.fromJson(new String(encoded, Charset.defaultCharset()), Config.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static List<Config> loadConfigs(String path) {
		Gson gson = new Gson();
		byte[] encoded;
		try {
			encoded = Files.readAllBytes(Paths.get(path));
			Type listType = new TypeToken<ArrayList<Config>>(){}.getType();
			return gson.fromJson(new String(encoded, Charset.defaultCharset()), listType);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not find the specified config file:\n" + path, "File Read Error", JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}

	public Config(String planetDataPath, String planetDataOutputPath, float globalPatchSizeMultiplier, float globalPatchSizeVariance, Ore[] ores, int maxOreTiles,
			int maxOrePatches, long seed, boolean countCurrent) {
		this.planetDataPath = planetDataPath;
		this.planetDataOutputPath = planetDataOutputPath;
		this.globalPatchSizeMultiplier = globalPatchSizeMultiplier;
		this.globalPatchSizeVariance = globalPatchSizeVariance;
		this.ores = ores;
		this.maxOreTiles = maxOreTiles;
		this.maxOrePatches = maxOrePatches;
		this.seed = seed;
		this.countCurrent = countCurrent;
	}
	
	public Config() {
		
	}
	
	public String toJSON() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(this);
	}
	
	public void cascadeOverrides() {
		if(!loadSurfaceHintMaps) {
			for(Ore ore : ores) {
				ore.surfaceHint = 0.0f;
			}
		}
	}
	
	public float getGlobalPatchSizeMultiplier() {
		return globalPatchSizeMultiplier;
	}

	public void setGlobalPatchSizeMultiplier(float globalPatchSizeMultiplier) {
		this.globalPatchSizeMultiplier = globalPatchSizeMultiplier;
	}

	public float getGlobalPatchSizeVariance() {
		return globalPatchSizeVariance;
	}

	public void setGlobalPatchSizeVariance(float globalPatchSizeVariance) {
		this.globalPatchSizeVariance = globalPatchSizeVariance;
	}

	public int getMaxOreTiles() {
		return maxOreTiles;
	}

	public void setMaxOreTiles(int maxOreTiles) {
		this.maxOreTiles = maxOreTiles;
	}

	public int getMaxOrePatches() {
		return maxOrePatches;
	}

	public void setMaxOrePatches(int maxOrePatches) {
		this.maxOrePatches = maxOrePatches;
	}

	public long getSeed() {
		return seed;
	}

	public void setSeed(long seed) {
		this.seed = seed;
	}

	public String getPlanetDataPath() {
		return planetDataPath;
	}

	public void setPlanetPath(String planetPath) {
		this.planetDataPath = planetPath;
	}

	public String getPlanetDataOutputPath() {
		return planetDataOutputPath;
	}

	public void setPlanetDataOutputPath(String planetDataOutputPath) {
		this.planetDataOutputPath = planetDataOutputPath;
	}
	
	public Ore[] getOres() {
		return ores;
	}

	public void setOres(Ore[] ores) {
		this.ores = ores;
	}
	
	public boolean isCountCurrent() {
		return countCurrent;
	}

	public void setCountCurrent(boolean countCurrent) {
		this.countCurrent = countCurrent;
	}

	public boolean isLoadSurfaceHintMaps() {
		return loadSurfaceHintMaps;
	}

	public void setLoadSurfaceHintMaps(boolean loadSurfaceHintMaps) {
		this.loadSurfaceHintMaps = loadSurfaceHintMaps;
	}

}

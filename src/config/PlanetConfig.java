package config;

public class PlanetConfig extends CommonConfig {
	public String name;
	public OreConfig[] ores;
	
	public void cascadeSettings(CommonConfig parent) {
		if(surfaceHintColour != -1) {
			surfaceHintColour = 0xFF000000 | (surfaceHintColour << 16);
		}
		super.cascadeSettings(parent);
		for(OreConfig ore : ores) {
			ore.makeColouredMaps = null;
			ore.surfaceHintMaps = null;
			ore.cascadeSettings(this);
		}
	}
}

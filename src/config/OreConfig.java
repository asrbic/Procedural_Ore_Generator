package config;

import static map.MapData.OPAQUE;

public class OreConfig extends CommonConfig {
	public String type = null;
	public int id;
	public int centreOreTile = -1;
	public Integer testColour = null;
	public OreConfig() {
		
	}
	
	public void cascadeSettings(CommonConfig other) {
		if(surfaceHintColour != -1) {
			surfaceHintColour = 0xFF000000 | (surfaceHintColour << 16);
		}
		super.cascadeSettings(other);
		if(makeColouredMaps != null && makeColouredMaps) {//get around lack of unsigned ints 
			testColour = Integer.parseInt(testColourHex, 16) | OPAQUE;
		}
		if(other instanceof OreConfig) {
			OreConfig otherOre = (OreConfig)other;
			if(type == null) {
				type = otherOre.type;
			}
			if(centreOreTile == -1) {
				centreOreTile = otherOre.centreOreTile;
			}
			if(testColour == null) {
				testColour = otherOre.testColour;
			}
		}
	}
}
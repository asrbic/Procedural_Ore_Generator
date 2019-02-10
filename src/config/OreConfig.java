package config;

import static map.MapData.OPAQUE;

public class OreConfig extends CommonConfig {
	public String type = null;
	public int id;
	public transient Integer testColour = null;
	
	public OreConfig() {
		
	}
	
	public void cascadeSettings(CommonConfig other) {
		if(surfaceHintColour != -1) {
			surfaceHintColour = 0xFF000000 | (surfaceHintColour << 16);
		}
		super.cascadeSettings(other);
		if(other instanceof OreConfig) {
			OreConfig otherOre = (OreConfig)other;
			if(type == null) {
				type = otherOre.type;
			}
		}
		if(testColourHex != null) {
			testColour = Integer.parseInt(testColourHex, 16) | OPAQUE;
		}
	}
	
	public String getOreName() {
		int suffixStartIndex = type.indexOf('_');
		if(suffixStartIndex != -1 && suffixStartIndex > 0) {
			return type.substring(0, suffixStartIndex);
		}
		else {
			return type;
		}
	}
}
package config;

public abstract class CommonConfig {

	
	public enum PlanetFace {
		FRONT(0, "front"), 
		LEFT(1, "left"), 
		RIGHT(2, "right"), 
		UP(3, "up"), 
		DOWN(4, "down"), 
		BACK(5, "back");
		
		public static final PlanetFace[] ALL = {PlanetFace.FRONT, PlanetFace.LEFT, PlanetFace.RIGHT, PlanetFace.UP, PlanetFace.DOWN, PlanetFace.BACK};

		public int index;
		public String name;
		PlanetFace(int index, String name) {
			this.index = index;
			this.name = name;
			
		}
	}

	public float surfaceAreaMultiplier = -1;
	public float surfaceAreaVariance = -1;
	public int maxOreTiles = -1;
	public int maxOrePatches = -1;
	public Long seed = null;
	public double p = -1;
	public float density = -1;
	public int shape = -1;
	public Boolean avoidIce = null;
	public int surfaceArea = -1;
	public Boolean surfaceHintMaps = null;
	public float surfaceHintProbability = -1;
	public int surfaceHintColour = -1;
	public Boolean makeColouredMaps = null;
	public String testColourHex = null;
	public int startDepth = -1;
	public int depth = -1;
	public String mappingFileTargetColour = null;
	public int mappingFileColourInfluence = -1;
	public int centreOreTile = -1;
	public PlanetFace[] planetFaces = null;
	
	public void cascadeSettings(CommonConfig other) {
		if(surfaceAreaMultiplier == -1) {
			surfaceAreaMultiplier = other.surfaceAreaMultiplier;
		}
		if(surfaceAreaVariance == -1) {
			surfaceAreaVariance = other.surfaceAreaVariance;
		}
		if(maxOreTiles == -1) {
			maxOreTiles = other.maxOreTiles;
		}
		if(maxOrePatches == -1) {
			maxOrePatches = other.maxOrePatches;
		}
		if(seed == null) {
			seed = other.seed;
		}
		if(p == -1) {
			p = other.p;
		}
		if(density == -1) {
			density = other.density;
		}
		if(shape == -1) {
			shape = other.shape;
		}
		if(avoidIce == null) {
			avoidIce = other.avoidIce;
		}
		if(surfaceArea == -1) {
			surfaceArea = other.surfaceArea;
		}
		if(surfaceHintMaps == null) {
			surfaceHintMaps = other.surfaceHintMaps;
		}
		if(surfaceHintProbability == -1) {
			surfaceHintProbability = other.surfaceHintProbability;
		}
		if(surfaceHintColour == -1) {
			surfaceHintColour = other.surfaceHintColour;
		}
		if(makeColouredMaps == null) {
			makeColouredMaps = other.makeColouredMaps;
		}
		if(testColourHex == null) {
			testColourHex = other.testColourHex;
		}
		if(startDepth == -1) {
			startDepth = other.startDepth;
		}
		if(depth == -1) {
			depth = other.depth;
		}
		if(mappingFileTargetColour == null) {
			mappingFileTargetColour = other.mappingFileTargetColour;
		}
		if(mappingFileColourInfluence == -1) {
			mappingFileColourInfluence = other.mappingFileColourInfluence;
		}
		if(centreOreTile == -1) {
			centreOreTile = other.centreOreTile;
		}
		if(planetFaces == null) {
			planetFaces = other.planetFaces;
		}
	}
	
	public void setDefaults() {
		surfaceAreaMultiplier = 1.0f;
		surfaceAreaVariance = 0.4f;
		maxOreTiles = 100000;
		maxOrePatches = 1000;
		seed = 7l;
		p = 0.0;
		density = 1.0f;
		shape = 1;
		avoidIce = true;
		surfaceArea = 20;
		surfaceHintMaps = true;
		surfaceHintProbability = 1.0f;
		surfaceHintColour = 128;
		makeColouredMaps = true;
		testColourHex = "FFFFFF";
		startDepth = 10;
		depth = 6;
		mappingFileTargetColour = "#616c83";
		mappingFileColourInfluence = 15;
		centreOreTile = -1;
		planetFaces = PlanetFace.ALL;
	}
}

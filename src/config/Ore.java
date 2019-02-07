package config;

public class Ore {
	public int id;
	public double occurenceProbility;
	public int surfaceArea;
	public float density;
	public int shape;
	public float surfaceHint;
	public boolean spawnOnIce;
	public int centreOreTile;
	public String testColour;
	//possibly set shaded surface patch visibility per ore? Could do a scalar (probability of showing patch). 
	//Would probs be useful to have an override in Config
	public Ore(int id, double occurenceProbability, int surfaceArea, float density, int shape, float surfaceHint, boolean spawnOnIce, int centreOreTile, String testColour) {
		this.id = id;
		this.occurenceProbility = occurenceProbability;
		this.surfaceArea = surfaceArea;
		this.density = density;
		this.shape = shape;
		this.surfaceHint = surfaceHint;
		this.spawnOnIce = spawnOnIce;
		this.centreOreTile = centreOreTile;
		this.testColour	= testColour;
	}

}
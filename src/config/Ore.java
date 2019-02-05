package config;

public class Ore {
	public final int id;
	public final double occurenceProbility;
	public final int surfaceArea;
	public final float density;
	public final int shape;
	public float surfaceHint;
	public final boolean spawnOnIce;
	//possibly set shaded surface patch visibility per ore? Could do a scalar (probability of showing patch). 
	//Would probs be useful to have an override in Config
	public Ore(int id, double occurenceProbability, int surfaceArea, float density, int shape, float surfaceHint, boolean spawnOnIce) {
		this.id = id;
		this.occurenceProbility = occurenceProbability;
		this.surfaceArea = surfaceArea;
		this.density = density;
		this.shape = shape;
		this.surfaceHint = surfaceHint;
		this.spawnOnIce = spawnOnIce;
	}

}
package core;

import com.google.common.collect.Table;
import com.google.common.collect.HashBasedTable;

public class Stats {
	
	Table<String, String, Counts> data;
	
	public Stats() {
		data = HashBasedTable.create();

	}
	
	public synchronized void add(String ore, String planet, int tiles) {
		Counts counts;
		if(tiles > 0) {
			if(data.contains(planet, "")) {
				counts = data.get(planet, "");
				data.put(planet, "", new Counts(counts, tiles, 1));
			}
			else {
				data.put(planet, "", new Counts(tiles,1));
			}
			
			if(data.contains("", ore)) {
				counts = data.get("", ore);
				data.put("", ore, new Counts(counts, tiles, 1));
			}
			else {
				data.put("", ore, new Counts(tiles,1));
			}
			
			if(data.contains(planet, ore)) {
				counts = data.get(planet, ore);
				data.put(planet, ore, new Counts(counts, tiles, 1));
			}
			else {
				data.put(planet, ore, new Counts(tiles,1));
			}
		}
	}
	
	public synchronized int getPlanetTileTotal(String planet) {
		int total = 0;
		if(data.contains(planet, "")) {
			total = data.get(planet, "").tiles;
		}
		return total;
	}
	
	public synchronized int getOreTileTotal(String ore) {
		int total = 0;
		if(data.contains("", ore)) {
			total = data.get("", ore).tiles;
		}
		return total;
	}
	
	public synchronized int getPlanetOreTileTotal(String planet, String ore) {
		int total = 0;
		if(data.contains(planet, ore)) {
			total = data.get(planet, ore).tiles;
		}
		return total;
	}
	
	public synchronized int getPlanetPatchTotal(String planet) {
		int total = 0;
		if(data.contains(planet, "")) {
			total = data.get(planet, "").patches;
		}
		return total;
	}
	
	public synchronized int getOrePatchTotal(String ore) {
		int total = 0;
		if(data.contains("", ore)) {
			total = data.get("", ore).patches;
		}
		return total;
	}
	
	public synchronized int getPlanetOrePatchTotal(String planet, String ore) {
		int total = 0;
		if(data.contains(planet, ore)) {
			total = data.get(planet, ore).patches;
		}
		return total;
	}
	
	public class Counts {
		int tiles;
		int patches;
		
		public Counts() {
			tiles = 0;
			patches = 0;
		}
		
		public Counts(int tiles, int patches) {
			this.tiles = tiles;
			this.patches = patches;
		}
		
		public Counts(Counts other, int tilesAdd, int patchesAdd) {
			this.tiles = other.tiles + tilesAdd;
			this.patches = other.patches + patchesAdd;
		}
	}
}

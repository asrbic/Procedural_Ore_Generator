# Procedural Ore Generator for Space Engineers

## Description

This program will randomly generate ore patches in PlanetDataFiles for existing planets in Space Engineers. The type, size, shape, denity etc of these ores can be configured for each planet. Ore templates can also be created at the global level and overwritten where needed. Optionally, the ids and other settings for each ore can also be populated in the PlanetGeneratorDefinitions.sbc file. 


## How to use

Requires Java version 1.8 or later.

1. Locate the PlanetDataFiles directory containing the planet data directories for the planets you want to use as a base
   - eg `C:\\Program Files (x86)\\Steam\\steamapps\\common\\SpaceEngineers\\Content\\Data\\PlanetDataFiles\\` 
2. Modify settings in config.json to suit your needs
   - planetDataPath should equal the path from step 1
   - The "name" for each planet config should be exactly the same as its corresponding directory name in the PlanetDataFiles from step 1
   - planetGeneratorDefinitionsPath (or planetGeneratorDefinitionsPathArray) should be the location of PlanetGeneratorDefinitions.sbc you want to use as a base
     - This is in the Data directory above the directory from step 1
   - All settings will cascade from oreTemplates -> all ores, global -> each planet then each planet -> its ores. Settings will not be overwritten if already set. This can minimise the amount of config you need to write.
     - eg. `"id": 1, "type": "Iron_02"` in an ore template will set all ores on all planets which have the id 1 to have the default type Iron_02
     - eg. "depth": 12 at the top level will set the default depth of all ores to 12
   - Be very careful with the "" {} [] , characters. Make sure brackets are always closed and all elements but the last have a comma at the end of the line
     - See https://www.json.org/
3. Run the program with run.bat
   - You might want to run with `"makeColouredMaps": true` until you get the output you want. Then run again with `"makeColouredMaps": false` or manually remove all of the coloured images from each planet directory
   - Writing/compressing pngs can take a surprisingly long time
4. Copy the contents to a new directory in `C:\Users\<username>\AppData\Roaming\SpaceEngineers\Mods\`
5. Add the directory you created to the mod list of a game in Space Engineers and test. You do not need to create a new world.
6. Select the directory containing your mod in the mod list and click Publish in the bottom right to upload it to the Steam Workshop
   - The program will print out a formatted table detailing the count of tiles generated for each type of ore (Everything between [table]...[/table]). You can paste this directly into the steam item description.

tl;dr: run.bat


## Configuration Options
- planetDataPath (default null): Directory of PlanetDataFiles containing data for each planet. If null, no ore generation will occur. 
- planetGeneratorDefinitionsPath (default null): Directory of PlanetGeneratorDefinitions.sbc to insert entries for configured ores. If null, no definitions file will be produced. Superseded by planetGeneratorDefinitionsPathArray but kept for older configs.
- planetGeneratorDefinitionsPathArray (default empty): Same as planetGeneratorDefinitionsPath but allows you to specify multiple .sbc files. All of the specified files will be searched for any planet config in config.json. 
- planetDataOutputPath (default "./PlanetDataFiles/"): Output path for planet data. Not used if no planetDataPath is not set. Directory will be created if it doesn't exist.
- planetGeneratorDefinitionsOutputPath (default "./"): Output path for specified .sbc file(s). Not used if planetGeneratorDefinitionsPath is not set. Directory will be created if it doesn't exist.
- makeColouredMaps (default true): Used at the global level to determine if colour coded test maps will be generated
- surfaceHintMaps (default true): Used at the global level to determine whether hint maps will be generated
- countExistingTiles (default true): If true, the existing ore tiles on the map will be counted before they get cleared

- maxOreTiles (default 100000): total maximum ore tiles/pixels to generate on a planet. The actual number could be less than this because overwritten tiles are still counted
- maxOrePatches (default 1000): total maximum ore patches to generate on a planet

- p (default 0.0): Chance of this ore being selected to spawn. effective chance to spawn is (total of all p on this planet)/p.
- surfaceArea (default 20): Maximum number of tiles in an ore patch. Each tiles is about 30x30 square metres on EarthLike. This will likely be different for different sized planets.
- density (default 1.0): Lower that 1 increases the area and reduces density. Higher is the opposite.
- startDepth (default 10): Starting depth. Ore will fill from this down "depth" metres.
- depth (default 6): Vertical size of the ore patch.
- shape (default 1): Determines the generator which will be used. See the Generator Shapes section for information on each one.
- patchSizeVariance (default 0.4): Add random variance to the surfaceArea.
- avoidIce (default true): If true, this ore will not spawn on/under ice lakes.
- centreOreTile (default -1): If set to a positive number, the given id will be used as the centre tile of the ore patch. This can be used to generate a single hint tile of ore close to the surface while the rest of the patch is far below, out of ore detector range. Just add the ore you want to be at the centre to the planet's config with "p": 0.0 so it doesn't spawn elsewhere.
- seed (default 7): Random seed used at the planet level. With the exact same configuration (including seed), the exact same ore patches should be generated. 
- surfaceAreaMultiplier (default: 1.0): Multiplier to increase/decrease surface area
- surfaceHintProbability (default 1.0): Chance that surface hints will show above each ore tile. Set to 0.0 to have no surface hint.
- surfaceHintColour (default 128): Colour of surface hints. This seems to change per planet/biome in vanilla.
- testColourHex (default "FFFFFF"): 24-bit hexadecimal rgb colour which this ore will show as in the colour coded test maps.
- mappingFileTargetColour (default "#616c83"): Required in PlanetGeneratorDefinitions.sbc for each ore. I'm not actually sure what this does but most of the vanilla ores have the default for this.
- mappingFileColourInfluence (default 15): Also unsure what this does. Similar usage in PlanetGeneratorDefinitions.sbc but is always 15.
- planetFaces (`default ["FRONT", "LEFT", "RIGHT", "UP", "DOWN", "BACK"]`): If not specified, the ore will generate on all faces of the planet. If specified, the ore will only be generated on a randomly selected face from the provided list. A value of `["UP","UP","LEFT"]` will give a 66% chance for the ore to spawn on the top of the planet and a 33% chance for it to spawn on the left face. A value of `["FRONT"]` will only allow the ore to spawn on the front face of the planet. A sphere only has one face but Space Engineers cuts it up into 6 sides for ore/material definition purposes. This field allows you to only spawn certain ores in certain areas of the planet, forcing the player to go to the far side to get the ore they need. 

## Generator Shapes

1. Random: Will randomly select one of the below for each patch. Excludes 7. because it doesn't quite look natural.
2. Gaussain random: Will randomly select tiles with a strong tendency towards the centre of the patch. This typically leads to a dense centre with some isolated ore tiles around it.
3. Snake: Works a bit like the game snake. One of 4 random directions (up, down, left, right) will be chosen. From this new tile, a random direction is chosen again. Repeat. This is the simplest algorithm and leads to shapes most like vanilla. 
4. Step Gaussian lines: Will randomly select tiles with a strong tendency towards the centre of the patch. A line is then drawn to it by stepping up, down, left or right. This is probably my favourite. 
5. Fuzzy linearly interpolated Gaussian lines: Will randomly select tiles with a strong tendency towards the centre of the patch and draw a line from the current tile to it. Leads to some intersting shapes but patches are often smaller than specified.
6. Circles: Draws circles shapes which tend to be more sparse the further from the centre the tile is. If density is 1.0, the circle will be solid and not sparse at all.
7. Sparse diamonds: Doesn't look natural, probably don't use. Ignores density.
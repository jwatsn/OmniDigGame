package com.jwatson.omnidig.liquid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.MathUtils;
import com.jwatson.omnidig.Configuration;
import com.jwatson.omnidig.Inventory.Items;
import com.jwatson.omnidig.Terrain.OmniTerrain;
import com.jwatson.omnidig.Terrain.TerrainChunk;
import com.jwatson.omnidig.ui.Console;

public class LiquidManager {


	static LiquidManager instance;

	//temp array
	byte[][] temp_array;

	//frustrum
	Frustum frustrum;

	public LiquidManager() {

		instance = this;

		temp_array = new byte[OmniTerrain.chunksize * 2][OmniTerrain.chunksize * 2];

	}
	public static void update_liquid() {

		float remaining = 0;

		for(TerrainChunk ch : OmniTerrain.instance.loadedChunks)
			System.arraycopy(ch.water_map, 0, ch.water_tempmap, 0, ch.water_map.length);

		for(TerrainChunk ch : OmniTerrain.instance.loadedChunks) {


			for(int x = 0; x<OmniTerrain.chunksize*2; x++) {
				for(int y = 0; y<OmniTerrain.chunksize*2; y++) {
					
					
					if(ch.map[x/2][y/2] == Items.MAT_Water) {
						
						ch.water_map[(x) + (y) * (OmniTerrain.chunksize*2)] = Configuration.Water_MaxBlocks;
						ch.water_map[(x+1) + (y) * (OmniTerrain.chunksize*2)] = Configuration.Water_MaxBlocks;
						ch.water_map[(x) + (y+1) * (OmniTerrain.chunksize*2)] = Configuration.Water_MaxBlocks;
						ch.water_map[(x+1) + (y+1) * (OmniTerrain.chunksize*2)] = Configuration.Water_MaxBlocks;
						ch.map[x/2][y/2] = Items.Empty;
					}

					boolean flag = false;

					int id = x + y * (OmniTerrain.chunksize*2);

					remaining = ch.water_map[id];

					if(remaining <= 0) continue;



					if(!isSolid(ch, x, y-1)) {

						float water = getWater(ch, x, y-1);
						if(water < Configuration.Water_MaxBlocks) {
							if(remaining + water <= Configuration.Water_MaxBlocks) {
								addWater(ch, x, y, -remaining);
								addWater(ch, x, y-1, remaining);
								remaining = 0;
							}
							else {
								float diff = Configuration.Water_MaxBlocks - water;
								diff = Math.min(diff, remaining);
								addWater(ch, x, y, -diff);
								addWater(ch, x, y-1, diff);
								remaining -= diff;
							}
						}
					}

					if(remaining <= 0.5f || flag) continue;

					if(!isSolid(ch, x+1, y)) {
						float water = getWater(ch, x+1, y);
						float flow = (remaining - water) / 2f;
						flow = MathUtils.clamp(flow, 0, remaining);
						addWater(ch, x, y, -flow);
						addWater(ch, x+1, y, flow);
						remaining -= flow;
					}

					if(remaining <= 0.5f) continue;

					if(!isSolid(ch, x-1, y)) {
						float water = getWater(ch, x-1, y);
						float flow = (remaining - water) / 2f;
						flow = MathUtils.clamp(flow, 0, remaining);
						addWater(ch, x, y, -flow);
						addWater(ch, x-1, y, flow);
						remaining -= flow;
					}



				}
			}

		}

		for(TerrainChunk ch : OmniTerrain.instance.loadedChunks)
			System.arraycopy(ch.water_tempmap, 0, ch.water_map, 0, ch.water_tempmap.length);
	}

	public static boolean isSolid(TerrainChunk ch, int x, int y) {

		if(y >= OmniTerrain.chunksize*2) {
			ch = OmniTerrain.chunks[ch.x2][ch.y2+1];
			y = 0;

			if(ch == null)
				return true; 
		}
		if(y < 0) {
			ch = OmniTerrain.chunks[ch.x2][ch.y2-1];
			y = (OmniTerrain.chunksize*2)-1;
			if(ch == null)
				return true;
		}
		if(x >= OmniTerrain.chunksize*2) {
			ch = OmniTerrain.chunks[ch.x2+1][ch.y2];
			x = 0;
		}
		if(x < 0) {
			ch = OmniTerrain.chunks[ch.x2-1][ch.y2];
			x = (OmniTerrain.chunksize*2) - 1;
		}

		return ch.map[x/2][y/2].type.isSolid();
	}

	static void setWater(TerrainChunk ch, int x, int y, float amt) {


		if(y >= OmniTerrain.chunksize*2) {
			ch = OmniTerrain.chunks[ch.x2][ch.y2+1];
			y = 0;

			if(ch == null)
				return; 
		}
		if(y < 0) {
			ch = OmniTerrain.chunks[ch.x2][ch.y2-1];
			y = (OmniTerrain.chunksize*2)-1;
			if(ch == null)
				return;
		}
		if(x >= OmniTerrain.chunksize*2) {
			ch = OmniTerrain.chunks[ch.x2+1][ch.y2];
			x = 0;
		}
		if(x < 0) {
			ch = OmniTerrain.chunks[ch.x2-1][ch.y2];
			x = (OmniTerrain.chunksize*2) - 1;        
		}

		ch.water_tempmap[x + y*(OmniTerrain.chunksize*2)] = amt;

	}

	static void addWater(TerrainChunk ch, int x, int y, float amt) {
		if(y >= OmniTerrain.chunksize*2) {
			ch = OmniTerrain.chunks[ch.x2][ch.y2+1];
			y = 0;

			if(ch == null)
				return; 
		}
		if(y < 0) {
			ch = OmniTerrain.chunks[ch.x2][ch.y2-1];
			y = (OmniTerrain.chunksize*2)-1;
			if(ch == null)
				return;
		}
		if(x >= OmniTerrain.chunksize*2) {
			ch = OmniTerrain.chunks[ch.x2+1][ch.y2];
			x = 0;
		}
		if(x < 0) {
			ch = OmniTerrain.chunks[ch.x2-1][ch.y2];
			x = (OmniTerrain.chunksize*2) - 1;        
		}

		ch.water_tempmap[x + y*(OmniTerrain.chunksize*2)] += amt;

	}

	public static float getWater(TerrainChunk ch, int x, int y) {

		if(y >= OmniTerrain.chunksize*2) {
			ch = OmniTerrain.chunks[ch.x2][ch.y2+1];
			y = 0;

			if(ch == null)
				return 0; 
		}
		if(y < 0) {
			ch = OmniTerrain.chunks[ch.x2][ch.y2-1];
			y = (OmniTerrain.chunksize*2)-1;

			if(ch == null)
				return 0; 
		}
		if(x >= OmniTerrain.chunksize*2) {
			ch = OmniTerrain.chunks[ch.x2+1][ch.y2];
			x = 0;
		}
		if(x < 0) {
			ch = OmniTerrain.chunks[ch.x2-1][ch.y2];
			x = (OmniTerrain.chunksize*2) - 1;
		}

		return ch.water_map[x + y*(OmniTerrain.chunksize*2)];
	}

}

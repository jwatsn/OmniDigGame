package com.jwatson.omnidig.Terrain;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.jwatson.omnidig.Configuration;
import com.jwatson.omnidig.Gfx.CustomBatch;
import com.jwatson.omnidig.Inventory.Items;
import com.jwatson.omnidig.Lighting.LightingManager;
import com.jwatson.omnidig.Player.Biome;
import com.jwatson.omnidig.World.OmniWorld;
import com.jwatson.omnidig.World.WorldObj;
import com.jwatson.omnidig.liquid.LiquidManager;
import com.jwatson.omnidig.netplay.Client;
import com.jwatson.omnidig.netplay.Server;
import com.jwatson.omnidig.ui.Console;

public class TerrainChunk {
	
	//Active flag
	public boolean isActive;
	
	//Active world objects
	public List<WorldObj> activeObjects;
	public List<WorldObj> toDelete;
	
	//testing collision
	public List<WorldObj> obj_grid[][];
	
	//Parent Terrain
	public OmniTerrain parent;
	
	//Tile damage
	public byte map_damage[][];
	
	//Lighting
	public byte lighting[][];
	public byte ambient[][];
	public byte light_flow[][];
	
	//Tile map
	public Items map[][];
	
	//water
	public float water_map[];
	public float water_tempmap[];
	
	public int size,x2,y2;
	int x,y;
	
	//loaded flag, mostly for network
	public boolean Loaded;
	
	int Hash;

	public byte BakedLightMap[];
	public byte LightMap2[];
	
	public TerrainChunk(OmniTerrain parent,int x, int y,int chunkSize) {
		
		this.parent = parent;
		
		this.x = x;
		this.y = y;
		this.x2 = x/chunkSize;
		this.y2 = y/chunkSize;
		
		this.size = chunkSize;
		
		map = new Items[chunkSize][chunkSize];
		map_damage = new byte[chunkSize][chunkSize];
		lighting = new byte[chunkSize][chunkSize];
		ambient = new byte[chunkSize][chunkSize];
		light_flow = new byte[chunkSize][chunkSize];
		
		water_map = new float[(chunkSize*2)*(chunkSize*2)];
		water_tempmap = new float[(chunkSize*2)*(chunkSize*2)];
		
		activeObjects = new ArrayList<WorldObj>();
		toDelete = new ArrayList<WorldObj>();
		
		SetUpObjectGrid();
		
		for(int xx=0; xx<chunkSize; xx++)
			for(int yx=0; yx<chunkSize; yx++)
				map_damage[xx][yx] = (byte)0;

		parent.loadedChunks.add(this);		
		isActive = true;
	}
	
	void SetUpObjectGrid() {
		
		
	}
	
	void fillMap(Items e) {
		
		for(int x=0; x<size; x++)
			for(int y=0; y<size; y++) {
				map[x][y] = e;
			}
		
	}
	
	public void render(CustomBatch batch, float delta) {
		
		for(int x=0; x<size; x++)
			for(int y=0; y<size; y++) {
				
				if(map[x][y] != null) {
					float bright = getBrightness(x, y);
					if(map[x][y] != Items.Empty) {
						
						if(map_damage[x][y] > 0) {
							
							
							int dmg = getDamage(x,y);
							
							
							if(parent.biomes[x2][y2] == Biome.Underground)
							batch.draw(Items.cave_wall.texture,bright, this.x + x, this.y + y,1,1);
							batch.draw(map[x][y].texture, map[x][y].type.damaged_alpha[dmg], bright,this.x + x, this.y + y,1,1);
						}
						else
							batch.draw(map[x][y].texture,bright, this.x + x, this.y + y,1,1);
					}
					else if(parent.biomes[x2][y2] == Biome.Underground)
						batch.draw(Items.cave_wall.texture,bright, this.x + x, this.y + y,1,1);
				}
				
				renderWater(batch,x,y);
			}
		
		parent.toRender.addAll(activeObjects);
		
	}
	
	void renderWater(CustomBatch batch, int x, int y) {
		
		for(int x2 = 0; x2 < 2; x2++)
			for(int y2 = 0; y2 < 2; y2++) {
				
				float water_x = (x*2) + x2;
				float water_y = (y*2) + y2;
				
				int id = (int)water_x + (int)water_y * (OmniTerrain.chunksize*2);
				
				if(water_map[id] > 0) {
					float a = (water_map[id] / Configuration.Water_MaxBlocks) * 8;
					a = ((float)MathUtils.round(a) / 8f) * 0.5f;
					if(a > 0.5f)
						a = 0.5f;
					float b = 0.5f;
					float offsetx = 0,offsety = 0;
					if(LiquidManager.getWater(this, (int)water_x, (int)water_y-1) < Configuration.Water_MaxBlocks)
						if(LiquidManager.getWater(this, (int)water_x, (int)water_y+1) > 0) {
							if(water_map[id] >= 1)
								a = 0.5f;
							else
								a = 0.25f;
						}
					
					batch.draw(Items.MAT_Water.texture, 1, this.x + (water_x/2f) + offsetx, this.y + (water_y/2f) + offsety,b,a);
				}
			}
		
	}
	
	
	int getDamage(int x, int y) {
		float a = (float)map_damage[x][y] / (float)map[x][y].type.maxhp;
		
		if(a > 1)
			a = 1;
		
		 
		
		return (int)(a * (map[x][y].type.damaged_alpha.length-1));
	}
	
	float getBrightness(int x, int y) {
			float a = (float)lighting[x][y] / (float)Configuration.Lighting_MaxBrightness;
			
			if(a > 1)
				a = 1;
			
			return a;
	}
	
	public void update(float delta) {
		
		
		int b = 0;
		for(WorldObj obj : activeObjects) {
			
			if(obj.removalFlag) {
				toDelete.add(obj);
				
				if(Server.isHosting)
					Server.instance.RemoveObject(obj);
				
			}
			
			if(obj.chunk == null)
				obj.chunk = this;
			
			
			
			if((int)obj.bounds.x / size != obj.chunk.x2 || (int)obj.bounds.y / size != obj.chunk.y2) {
				parent.exitingChunk.add(obj);
				
			}
			
			obj.update(delta);
			
		}
		
		if(toDelete.size() > 0) {
			activeObjects.removeAll(toDelete);
			OmniWorld.instance.spawnedObjects.removeAll(toDelete);
			toDelete.clear();			
		}
		
	}
	
	public void generate(Biome b) {
		
		Items[] grass = new Items[1];
		Items[] dirt = new Items[1];
		
		grass[0] = Items.MAT_Grass;
		dirt[0] = Items.MAT_Dirt;
		
		switch(b) {
		
		case Sky:
			fillMap(Items.Empty);
			break;
			
		case Underground:
			GenerateUnderground();
			break;
			
		case Forest:
			
			
			map = BiomeManager.GetForestBiome(parent.chunksize, parent.chunksize, 4, grass, dirt, Items.MAT_Dirt, 5, 3);
			
			ambient = LightingManager.instance.createSkyAmbient(this);

			
			break;
		case ForestCL:
			map = BiomeManager.generateCorner(parent.chunksize, parent.chunksize-1, 0, Items.MAT_Grass, Items.MAT_Dirt);
			ambient = LightingManager.instance.createSkyAmbient(this);
			break;
		case ForestCR:
			map = BiomeManager.generateCorner(parent.chunksize, 0, parent.chunksize-1, Items.MAT_Grass, Items.MAT_Dirt);
			ambient = LightingManager.instance.createSkyAmbient(this);
			break;
		}
		
		
		Loaded = true;
		
	}
	
	void GenerateUnderground() {
		
		Vector3[] materials = {new Vector3(Items.MAT_Coal.ordinal(),4,3),
							  new Vector3(Items.MAT_Copper.ordinal(),5,3),
							  new Vector3(Items.MAT_Stone.ordinal(), 9, 4)
							  };
		
				
			map = BiomeManager.GetUndergroundBiome(parent.chunksize, parent.chunksize,55, Items.MAT_Dirt, materials);

		
	}

	public void activate() {
		parent.loadedChunks.add(this);
		isActive = true;
		
	}
	

}

package com.jwatson.omnidig.Terrain;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.jwatson.omnidig.Configuration;
import com.jwatson.omnidig.Camera.OmniCam;
import com.jwatson.omnidig.Gfx.CustomBatch;
import com.jwatson.omnidig.Inventory.Block;
import com.jwatson.omnidig.Inventory.ItemObject;
import com.jwatson.omnidig.Inventory.Items;
import com.jwatson.omnidig.Lighting.LightingManager;
import com.jwatson.omnidig.Listeners.BlockUpdateListener;
import com.jwatson.omnidig.Player.Biome;
import com.jwatson.omnidig.Player.Player;
import com.jwatson.omnidig.World.OmniWorld;
import com.jwatson.omnidig.World.WorldObj;
import com.jwatson.omnidig.events.BlockUpdateEvent;
import com.jwatson.omnidig.events.EventManager;
import com.jwatson.omnidig.liquid.LiquidManager;
import com.jwatson.omnidig.netplay.Client;
import com.jwatson.omnidig.netplay.Server;
import com.jwatson.omnidig.netplay.Client.ServerCommand;
import com.jwatson.omnidig.ui.Console;



public class OmniTerrain {
	
	//List for blocks to be changed
	public List<TerrainChunk> loadedChunks;
	
	public static TerrainChunk[][] chunks;
	
	public static OmniTerrain instance;
	
	public Biome[][] biomes;
	
	public static int width,height,chunksize;
	
	//list for rendering
	public List<WorldObj> toRender;
	
	//World object stuff
	public List<WorldObj> exitingChunk;
	
	//Culling vars
	Frustum frustrum;
	Vector3 culling;
	
	//Liquid stuff
	int water_timer;
	
	
	public OmniTerrain(int width, int height, int chunksize) {
		
		if(instance != null)
			instance = null;
		
		instance = this;
		
		this.width = width;
		this.height = height;
		this.chunksize = chunksize;
		
		initVars();
		SetUpListeners();
		
	}
	
	void initVars() {
		
		
		culling = new Vector3();
		frustrum = OmniCam.getFrustrum();
		
		
		
		chunks = new TerrainChunk[width][height];
		biomes = new Biome[width][height];
		
		exitingChunk = new ArrayList<WorldObj>();
		toRender = new ArrayList<WorldObj>();
		loadedChunks = new ArrayList<TerrainChunk>();
		
		
		
	}

	public void render(CustomBatch batch, float delta) {

		if(Client.isConnected && !Client.isSpawned)
			return;
		
		for(int x=(int)frustrum.planePoints[0].x/chunksize; x < frustrum.planePoints[1].x/chunksize; x++)
			for(int y=(int)frustrum.planePoints[0].y/chunksize; y < frustrum.planePoints[2].y/chunksize; y++)
				{
				
						if(chunks[x][y] == null)
							if(x>=0 && x < width && y >= 0 && y < height) {
								
								
								chunks[x][y] = new TerrainChunk(this,x*chunksize, y*chunksize, chunksize);
								if(Client.isSpawned)
									Client.instance.RequestChunk(x, y);
								
								if(!Client.isConnected)
								chunks[x][y].generate(biomes[x][y]);

							}

						chunks[x][y].render(batch, delta);
						
										// Creates new terrain if not a client
						
						
				}
		
		for(WorldObj obj : toRender) {
			obj.render(batch, delta);
		}
		toRender.clear();

	}

	public void update(float delta) {
		boolean flag = false;
		
				
		
		if(!exitingChunk.isEmpty()) {
		for(WorldObj obj : exitingChunk) {
			
			obj.chunk.activeObjects.remove(obj);
			
			obj.chunk = chunks[(int)(obj.bounds.x)/chunksize][(int)(obj.bounds.y)/chunksize];
			
			if(Client.isSpawned)
				if(obj.chunk == null) {
					chunks[(int)(obj.bounds.x)/chunksize][(int)(obj.bounds.y)/chunksize] = new TerrainChunk(this, (int)((obj.bounds.x)/chunksize)*chunksize, (int)((obj.bounds.y)/chunksize)*chunksize, chunksize);
					obj.chunk = chunks[(int)(obj.bounds.x)/chunksize][(int)(obj.bounds.y)/chunksize];
					
				}
			
			obj.chunk.activeObjects.add(obj);

		}
		exitingChunk.clear();
		}
		
		if(water_timer >= Configuration.Water_UpdateSpeed) {
			for(int i=0; i<2; i++) {
			LiquidManager.update_liquid();
			}
			water_timer = 0;
		}
		
		for(TerrainChunk ch : loadedChunks)
			ch.update(delta);
		
		water_timer++;
		
	}
	
	public void generateTerrain() {
		
		
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++) {
				
				if(y > height/2) {
					biomes[x][y] = Biome.Sky;
				}
				else if(y == (height/2)) {
					biomes[x][y] = Biome.Forest;
				}
				else {
					biomes[x][y] = Biome.Underground;
				}
				
				
			}
		
		biomes = BiomeManager.generateMountains(biomes, (height/2), Biome.Forest,Biome.ForestCL,Biome.ForestCR, 80);
		
		 
		
	}
	
	void SetUpListeners() {
		
		BlockUpdateListener blockUpdate = new BlockUpdateListener() {
			@Override
			public void onUpdate(BlockUpdateEvent e) {
				switch(e.updateType) {
				case Damaged:
					damageBlock(e.owner,e.x, e.y, e.type);
					break;
				case Deleted:
					deleteBlock(e.x, e.y);
					break;
				case Created:
					PlaceBlock(e.x, e.y, e.type);
					break;
				case Update:
					setBlock(e.x, e.y, e.type, e.hp);
					break;
				}
			}
		};
		
		EventManager.addListener(blockUpdate);
		
	}
	
	public static Items getBlock(int x, int y) {	
		return chunks[x/chunksize][y/chunksize].map[x%chunksize][y%chunksize];
	}
	
	public boolean addObject(WorldObj obj) {
		
		if(Client.isConnected && !Client.isSpawned)
			return false;
		
		try {
			int x = (int)(obj.bounds.x/chunksize);
			int y = (int)(obj.bounds.y/chunksize);
			
			if(Client.isSpawned)
				if(chunks[x][y] == null) {
					chunks[x][y] = new TerrainChunk(this, x * chunksize, y * chunksize, chunksize);
					Client.instance.RequestChunk(x, y);					
				}
			
			if(chunks[x][y] != null)
				if(chunks[x][y].Loaded) {
					chunks[x][y].activeObjects.add(obj);
					return true;
				}

			return false;
			
			
		}
		catch(Exception e) {
			Console.AddLine("Failed to add object: "+e.toString());
			
		}
		
		return false;
		
	}
	
	public static boolean isSolid(int x, int y) {
		
		if(x < 0 || y < 0)
			return true;
		
		if(chunks[x/chunksize][y/chunksize] != null) {
			
			if(chunks[x/chunksize][y/chunksize].map[x%chunksize][y%chunksize] == null)
				return false;
			
			return chunks[x/chunksize][y/chunksize].map[x%chunksize][y%chunksize].type.isSolid();
		}
		else
			return true;
	
	}
	
public static boolean isSolid(Items item,int x, int y) {
		
		if(x < 0 || y < 0)
			return true;
		
		int chX = x/chunksize;
		int chY = y/chunksize;
		int mapX = x%chunksize;
		int mapY = y%chunksize;
		
		if(chunks[chX][chY] != null) {
			
			if(chunks[chX][chY].map[mapX][mapY] == null)
				return false;
			
			
			
			return chunks[chX][chY].map[mapX][mapY].type.isSolid();
		}
		else
			return true;
	
	}
	
	public static boolean isSolid(int chX, int chY,int mapX, int mapY) {
		
		if(mapX < 0) {
			mapX = chunksize - 1;
			chX --;
		}
		else if(mapX >= chunksize) {
			mapX = 0;
			chX ++;
		}
		if(mapY < 0) {
			mapY = chunksize - 1;
			chX --;
		}
		else if(mapY >= chunksize) {
			mapY = 0;
			chY ++;
		}
		
		if(chunks[chX][chY] != null) {
			
			if(chunks[chX][chY].map[mapX][mapY] == null)
				return false;
			
			return chunks[chX][chY].map[mapX][mapY].type.isSolid();
		}
		else
			return true;
	
	}
	
	
	
	public static void setBlock(int x, int y, Items item, int hp) {
		
		int x2 = x / chunksize;
		int y2 = y / chunksize;
		int x3 = x % chunksize;
		int y3 = y % chunksize;
		
		OmniTerrain.setBlock(x2, y2, x3, y3, item, hp);
	}
	
	public static void setBlock(int x,int y,int x2, int y2, Items item, int hp) {
		
			if(chunks[x][y] == null)
				return;
			
			chunks[x][y].map[x2][y2] = item;
			chunks[x][y].map_damage[x2][y2] = (byte)hp;
			
	}
	
	public static boolean isBlock(int x, int y) {
		
		if(chunks[x/chunksize][y/chunksize] != null) {
			
			if(chunks[x/chunksize][y/chunksize].map[x%chunksize][y%chunksize] == null)
				return false;
			
			return (chunks[x/chunksize][y/chunksize].map[x%chunksize][y%chunksize].type.getClass() == Block.class);
		}
		else
			return true;
	}
	
	public static void deleteBlock(int x, int y) {
		
		int x2 = x/chunksize;
		int y2 = y/chunksize;
		int x3 = x%chunksize;
		int y3 = y%chunksize;
		
		
		if(chunks[x2][y2] != null) {
			
			if(chunks[x2][y2].map[x3][y3] == null)
				return;
			
			
			if(!Client.isConnected) {
			ItemObject obj = new ItemObject(chunks[x2][y2].map[x3][y3], 1);				
			OmniWorld.instance.spawnObject(obj, (x + 0.2f) + 0.25f, y + 0.25f, 0.5f, 0.5f);
			obj.width = 0.5f;
			obj.height = 0.5f;
			obj.bounds.width = 0.5f;
			obj.bounds.height = 0.5f;
			obj.playAnim(obj.item.name());			
			}
			chunks[x2][y2].map[x3][y3] = Items.Empty;
	
			
		}
			
	}
	
	public static void setDamageBlock(int x, int y, byte amt) {
		
		int x2 = x/chunksize;
		int y2 = y/chunksize;
		int x3 = x%chunksize;
		int y3 = y%chunksize;
		
		if(chunks[x2][y2].map[x3][y3] == null)
			return;
		
		chunks[x2][y2].map_damage[x3][y3] = amt;
		
	}
	
	public static void damageBlock(Player player,int x, int y, Items item) {
		
		int x2 = x/chunksize;
		int y2 = y/chunksize;
		int x3 = x%chunksize;
		int y3 = y%chunksize;
		
		if(chunks[x2][y2].map[x3][y3] == null)
			return;
		
		if(chunks[x2][y2].map_damage[x3][y3] < chunks[x2][y2].map[x3][y3].type.maxhp)
		chunks[x2][y2].map_damage[x3][y3]++;
		
		
		
		if(chunks[x2][y2].map_damage[x3][y3] >= chunks[x2][y2].map[x3][y3].type.maxhp) {
			
			BlockUpdateEvent event = BlockUpdateEvent.allocate();
			event.set(player,OmniWorld.ticks,x,y,null,1,BlockUpdateEvent.UpdateType.Deleted);
			EventManager.addEvent(event);
			
		}
	}
	
	public static boolean canPlace(int x, int y) {
		
		int x2 = x/chunksize;
		int y2 = y/chunksize;
		int x3 = x%chunksize;
		int y3 = y%chunksize;
		
		TerrainChunk ch = chunks[x2][y2];
		if(ch == null)
			return false;
		
		if(chunks[x2][y2].map[x3][y3] != Items.Empty) 
			return false;
		
		
		Rectangle r = new Rectangle(x, y, 1, 1);
		for(WorldObj obj : ch.activeObjects) {
			if(r.overlaps(obj.bounds))
				return false;
		}
		
		return true;
	}
	
	public static boolean PlaceBlock(int x, int y, Items item) {
		
		
		
		int x2 = x/chunksize;
		int y2 = y/chunksize;
		int x3 = x%chunksize;
		int y3 = y%chunksize;
		
		chunks[x2][y2].map[x3][y3] = item;
		chunks[x2][y2].map_damage[x3][y3] = 0;

		return true;
	}

	public static void genChunk(int x, int y) {
		
		TerrainChunk ch = new TerrainChunk(instance, x*chunksize, y*chunksize, chunksize);
		ch.generate(instance.biomes[x][y]);
		
		chunks[x][y] = ch;
		
		
	}
	
	int[] ret = {0,0,0,0}; //goes like this, chunkX,chunkY,localmapX,localmapY
	public static int[] convertCoords(float x,float y) {
		
		if(instance == null)
			return null;
		
		instance.ret[0] = (int) (x/chunksize);
		instance.ret[1] = (int) (y/chunksize);
		
		instance.ret[2] = (int) (x%chunksize);
		instance.ret[3] = (int) (y%chunksize);
		
		return instance.ret;
		
	}

	public static boolean isOpaque(int x, int y, int x2, int y2) {
		// TODO Auto-generated method stub
		return false;
	}
	

}

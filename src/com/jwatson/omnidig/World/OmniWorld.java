package com.jwatson.omnidig.World;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.jwatson.omnidig.OmniInput;
import com.jwatson.omnidig.Camera.OmniCam;
import com.jwatson.omnidig.Gfx.CustomBatch;
import com.jwatson.omnidig.Inventory.Items;
import com.jwatson.omnidig.OmniInput.input;
import com.jwatson.omnidig.Lighting.LightingManager;
import com.jwatson.omnidig.Managers.InputManagerOld;
import com.jwatson.omnidig.Managers.LocalInput;
import com.jwatson.omnidig.Player.CommandHandler;
import com.jwatson.omnidig.Player.Player;
import com.jwatson.omnidig.Player.PlayerClient;
import com.jwatson.omnidig.Screens.GameScreen;
import com.jwatson.omnidig.Terrain.OmniTerrain;
import com.jwatson.omnidig.Terrain.TerrainChunk;
import com.jwatson.omnidig.events.EventManager;
import com.jwatson.omnidig.netplay.Client;
import com.jwatson.omnidig.netplay.Server;
import com.jwatson.omnidig.parallax.BackgroundManager;
import com.jwatson.omnidig.shaders.ShaderManager;
import com.jwatson.omnidig.ui.Console;



public class OmniWorld extends InputListener {
	
	public static int SpawnX,SpawnY;
	
	public final static float TICK_RATE = 0.03f;
	
	public static boolean Paused;
	
	public static long ticks;
	
	
	public static OmniWorld instance;
	
	
	float tick_timer;
	
	//Player
	public Player client;
	public LocalInput localInput;
	
	
	//Spawning Queue
	public List<WorldObj> spawnedObjects;
	List<WorldObj> spawnObjs;
	List<WorldObj> delObjs;
	
	public OmniTerrain Terrain;
	CustomBatch batch;
	
	GameScreen scrn;
	
	Stage worldStage;
	
	//Parralax background
	BackgroundManager bgManager;
	
	
	public OmniWorld(GameScreen scrn) {
		
		if(instance != null)
			instance = null;
		
		instance = this;
		
		OmniCam.CreateCam();
		
		bgManager = new BackgroundManager(OmniCam.getCamera());
		
		worldStage = new Stage();
		 
		
		//Setting up the world object stuff
		Player.spawnedPlayers = new ArrayList<Player>();
		spawnedObjects = new ArrayList<WorldObj>();
	    spawnObjs = new ArrayList<WorldObj>();
		delObjs = new ArrayList<WorldObj>();
		ShaderProgram shader = new ShaderProgram(ShaderManager.terrain_vertex, ShaderManager.terrain_frag);
		batch = new CustomBatch(256,shader);
		
		Terrain = new OmniTerrain(1000, 1000, 10);
		Terrain.generateTerrain();
		
		client = new Player();
		localInput = new LocalInput(client);
		
		
		spawnObject(client, SpawnX, SpawnY, 0.60f, 1);
		client.AddItemToBag(Items.TOOL_PickAxe_Wood, 1);
		client.AddItemToBag(Items.MAT_Water, 1);
		client.AddItemToBag(Items.WEP_Sword_Wood, 1);
		client.activeManagers.add(new InputManagerOld(client));
		
		this.scrn = scrn;
		scrn.stage.addListener(new OmniInput());
		Gdx.input.setInputProcessor(scrn.stage);
		
		
		
		OmniCam.SetOwner(client);
	}
	
	public void update(float delta) {
		
		
		EventManager.processEvents(ticks);
		
		checkSpawnQueue();
		
		
		
		
		Terrain.update(delta);
		
		
		
		ticks++;
		
	}
	
	float fps_timer;
	int counter;
	
	public void render(float delta) {
		
		
		bgManager.render();
		
		
		
		batch.setProjectionMatrix(OmniCam.getCombined());
		
		batch.begin();
		Terrain.render(batch, delta);
		batch.end();
		
		counter++;
		fps_timer += delta;
		
		if(fps_timer >= 1) {
			//Gdx.app.debug("FPS:", " "+counter);
			counter = 0;
			fps_timer = 0;
		}
	}
	
	void checkSpawnQueue() {
		
		if(!spawnObjs.isEmpty()) {
			for(int i=0; i<spawnObjs.size(); i++) {
				if(Terrain.addObject(spawnObjs.get(i))) {
					spawnedObjects.add(spawnObjs.get(i));
					spawnObjs.remove(i);
				}
			}
		}
		
	}
	
	
	public WorldObj spawnObject(WorldObj obj, float x, float y, float width, float height) {
		
		obj.lastbounds.set(x,y);
		obj.pos.set(x, y);
		obj.bounds.x = x;
		obj.bounds.y = y;
		
		spawnObjs.add(obj);
		
		
		return obj;
		
	}
	
	List<input> inputBuffer = new ArrayList<OmniInput.input>();
	
	

	
	public void resetMap() {
		
		
		spawnedObjects.clear();
		EventManager.clearE();
		Player.spawnedPlayers.clear();
		Terrain = new OmniTerrain(OmniTerrain.width, OmniTerrain.height, OmniTerrain.chunksize);
		
	}
	
	

}

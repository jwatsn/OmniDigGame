package com.jwatson.omnidig.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.jwatson.omnidig.Configuration;
import com.jwatson.omnidig.OmniInput;
import com.jwatson.omnidig.Assets.AssetManager;
import com.jwatson.omnidig.Camera.OmniCam;
import com.jwatson.omnidig.Inventory.Items;
import com.jwatson.omnidig.Lighting.LightingManager;
import com.jwatson.omnidig.Managers.LocalInput;
import com.jwatson.omnidig.OmniScript.OmniScript;
import com.jwatson.omnidig.World.OmniWorld;
import com.jwatson.omnidig.events.EventManager;
import com.jwatson.omnidig.liquid.LiquidManager;
import com.jwatson.omnidig.netplay.Client;
import com.jwatson.omnidig.netplay.Server;
import com.jwatson.omnidig.ui.Chat;
import com.jwatson.omnidig.ui.Console;
import com.jwatson.omnidig.ui.InventoryManager;
import com.jwatson.omnidig.ui.MessageBox;
import com.jwatson.omnidig.ui.OnScreenNumpad;
import com.jwatson.omnidig.ui.OnScreenText;
import com.jwatson.omnidig.ui.QuickBar;

public class GameScreen implements Screen {
	
	
	
	//static inventory manager
	public static InventoryManager invManager;
	
	//Net play stuff
	Server server;
	Client client;
	
	OmniWorld World;
	public Stage stage;
	
	public static float worldUpdateTimer;
	float serverUpdateTimer;
	float clientUpdateTimer;
	
	//fps display stuff
	public static int fps;
	int fps_counter;
	float fps_timer;
	

	
	public static float elapsed = 0;
	public static float lastTick = 0;

	@Override
	public void render(float delta) {
		
		
		stage.act();
		

		
		worldUpdateTimer += delta;
		serverUpdateTimer += delta;
		clientUpdateTimer += delta;
		elapsed += delta;
		
		
		
		OmniCam.UpdateCam();
		
		
		// Update world
		while(worldUpdateTimer >= Configuration.TICK_RATE) {
					
		if(Server.isHosting) {
			server.update(Configuration.TICK_RATE);
		}
	
		World.localInput.update();	
		if(Client.isConnected) {
			client.update();
		}
		World.update(Configuration.TICK_RATE);
		
		worldUpdateTimer -= Configuration.TICK_RATE;
		
		
		
		}
		
		
		
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);		
		//Render World
		World.render(delta);	
		//renders UI
		stage.draw();		
		fps_timer += delta;
		if(fps_timer >= 1) {			
			fps = Integer.valueOf(fps_counter);			
			fps_timer = 0;
			fps_counter = 0;						
		}		
		fps_counter++;
	}

	
	
	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void show() {
		// Create the terrain and stuff
		OmniInput.initialize();
		
		server = new Server();
		client = new Client();
		
		OmniScript.startScriptEngine();
		
		AssetManager.Create();
		stage = new Stage(480,320,false);
		
		new OnScreenNumpad(stage);
		
		Console console = new Console(stage);
		invManager = new InventoryManager(stage);
		MessageBox msgbox = new MessageBox(stage);
		QuickBar qb = new QuickBar(stage);
		
		Chat chatbox = new Chat(stage);
		
		stage.addActor(invManager);
		stage.addActor(console);
		stage.addActor(msgbox);
		stage.addActor(qb);
		stage.addActor(new EventManager());
		//stage.addActor(chatbox);
		
		invManager.setZIndex(0);
		chatbox.setZIndex(0);
		qb.setZIndex(0);
		msgbox.setZIndex(0);
		console.setZIndex(0);
		console.setVisible(false);
		
		stage.addActor(new OnScreenText());
		World = new OmniWorld(this);
		
		stage.addListener(World);
		
		LightingManager lighting = new LightingManager();
		new LiquidManager();
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

}

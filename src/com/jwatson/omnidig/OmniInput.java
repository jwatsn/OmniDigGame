package com.jwatson.omnidig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.jwatson.omnidig.Camera.OmniCam;
import com.jwatson.omnidig.Inventory.Items;
import com.jwatson.omnidig.Managers.InputManagerOld;
import com.jwatson.omnidig.Player.PlayerClient;
import com.jwatson.omnidig.Terrain.OmniTerrain;
import com.jwatson.omnidig.World.OmniWorld;
import com.jwatson.omnidig.events.EventManager;
import com.jwatson.omnidig.events.TouchEvent;
import com.jwatson.omnidig.netplay.Server;
import com.jwatson.omnidig.ui.Console;

public class OmniInput extends InputListener {
	
	public class input {
	
	public Vector2 vec,accel;
	public float x,y;
	public int id;
	public int keycode;
	public int index;
	public long tick;
	public long origTick;
	
		public input(int id, int keycode, long tick) {
			this.id = id;
			this.keycode = keycode;
			this.tick = tick;
			vec = new Vector2();
			accel = new Vector2();
			this.index = inputIndex;
			inputIndex++;
		}

	}
	

	
	public static int inputIndex = 0;
	
	public static List<input> inputs;
	
	int[] pressed_keys = {101, 101};
	
	public static Map<Integer, String> Commands;
	
	public static OmniInput instance;

	int numKeysDown;
	
	public OmniInput() {
			
			
		
		
		inputs = new ArrayList<input>();		
		Commands = new Hashtable<Integer, String>();
		

		
		
		
	}
	
	
	
	
	public static void initialize() {
		if(instance != null)
			instance = null;
		
		instance = new OmniInput();
	}
	
	
	
	@Override
	public boolean keyDown(InputEvent event, int keycode) {
		// TODO Auto-generated method stub
		
//		boolean flag = false;
//		
//		for(int i=0; i<pressed_keys.length;i++) {
//			if(pressed_keys[i] > 100) {
//				pressed_keys[i] = keycode;
//				flag = true;
//				break;
//			}
//		}
//		
//		
//		if(flag)
//		if(keycode < 80) //limiting keys that will be send
//		inputs.add(new input(OmniWorld.instance.client.id,keycode,OmniWorld.ticks));
		
		if(keycode == Keys.A) {
			((InputManagerOld)OmniWorld.instance.client.getManager(InputManagerOld.class)).leftPressed = true;
		}
		else if(keycode == Keys.D)
			((InputManagerOld)OmniWorld.instance.client.getManager(InputManagerOld.class)).rightPressed = true;
		else if(keycode == Keys.W)
			((InputManagerOld)OmniWorld.instance.client.getManager(InputManagerOld.class)).upPressed = true;

		return super.keyDown(event, keycode);
	}
	
	@Override
	public boolean keyUp(InputEvent event, int keycode) {
		// TODO Auto-generated method stub
		
//		boolean flag = false;
//		
//		for(int i=0; i<pressed_keys.length; i++) {
//			if(pressed_keys[i] == keycode) {
//				pressed_keys[i] = keycode*100 ;
//				flag = true;
//				break;
//			}
//		}
//		
//		if(flag)
//		if(keycode < 80)
//		inputs.add(new input(OmniWorld.instance.client.id,keycode*100,OmniWorld.ticks));
		
		if(keycode == Keys.A) {
			((InputManagerOld)OmniWorld.instance.client.getManager(InputManagerOld.class)).leftPressed = false;
		}
		else if(keycode == Keys.D)
			((InputManagerOld)OmniWorld.instance.client.getManager(InputManagerOld.class)).rightPressed = false;
		
		else if(keycode == Keys.W)
			((InputManagerOld)OmniWorld.instance.client.getManager(InputManagerOld.class)).upPressed = false;
		
		return super.keyUp(event, keycode);
	}
	
	
	
	@Override
	public boolean touchDown(InputEvent event, float x, float y, int pointer,
			int button) {
		// TODO Auto-generated method stub
		
		if(y < 70)
			return false;
		
		
	
		InputManagerOld inputManager = (InputManagerOld)OmniWorld.instance.client.getManager(InputManagerOld.class);
		
		
		inputManager.isTouched = true;
		
		Hax(x, y, pointer, button);
		
		return true;
	}
	
	@Override
	public void touchUp(InputEvent event, float x, float y, int pointer,
			int button) {
		// TODO Auto-generated method stub
		
		InputManagerOld inputManager = (InputManagerOld)OmniWorld.instance.client.getManager(InputManagerOld.class);
		inputManager.isTouched = false;
		
		super.touchUp(event, x, y, pointer, button);
	}
	
	
	Vector3 transform = new Vector3();
	void Hax(float x, float y, int pointer,int button) {
		
			
		
			if(Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) {
				
				
				transform.set(Gdx.input.getX(),Gdx.input.getY(),0);
				OmniCam.instance.camera.unproject(transform);
			
			if(button == 0) {
				
				if(!OmniTerrain.isSolid((int)transform.x, (int)transform.y)) {
				
				OmniWorld.instance.client.bounds.x = (int)transform.x;
				OmniWorld.instance.client.bounds.y = (int)transform.y;
				
				}
			}
			else if(button == 1) {
				
				if(Server.isHosting) {
					if(!OmniTerrain.isSolid((int)transform.x, (int)transform.y)) {
						
						for(PlayerClient cl : Server.instance.Clients.values()) {
							cl.bounds.x = (int)transform.x;
							cl.bounds.y = (int)transform.y;
							Server.instance.ForceMove(cl);
						}
						
					}
				}
				
			}
			
			
			
		}
	}
	
	@Override
	public boolean scrolled(InputEvent event, float x, float y, int amount) {
		// TODO Auto-generated method stub
		
		
		
		if(Gdx.input.isKeyPressed(Keys.CONTROL_LEFT))
			OmniCam.instance.camera.zoom += amount;
		else
			OmniCam.instance.camera.zoom += amount * 0.15f;
		
		if(OmniCam.instance.camera.zoom < 0.5f)
			OmniCam.instance.camera.zoom = 0.5f;
		
			
		return super.scrolled(event, x, y, amount);
	}

	
	void addKey(int id, long ticks, int keycode, int index) {
		
		OmniInput.input key = new input(id, keycode, ticks);
		key.index = index;
		inputs.add(key);
		
	}
	
	void addKey(int id, long ticks, long origtick, int keycode, int index) {
		
		OmniInput.input key = new input(id, keycode, ticks);
		key.index = index;
		key.origTick = origtick;
		inputs.add(key);
		
	}
	
	public static void AddKey(int id, long ticks,long origtick, int keycode, int index) {
		
		instance.addKey(id, ticks, origtick, keycode, index);
		
	}

	public void update(float delta) {
		
		
		
	}
}

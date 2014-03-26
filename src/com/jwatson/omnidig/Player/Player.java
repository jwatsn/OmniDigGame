package com.jwatson.omnidig.Player;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.omg.CORBA.portable.ValueOutputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.jwatson.omnidig.Configuration;
import com.jwatson.omnidig.OmniInput;
import com.jwatson.omnidig.Listeners.InputListener;
import com.jwatson.omnidig.Listeners.TouchListener;
import com.jwatson.omnidig.OmniInput.input;
import com.jwatson.omnidig.Assets.AssetManager;
import com.jwatson.omnidig.Inventory.ItemObject;
import com.jwatson.omnidig.Inventory.ItemType.Swing_Type;
import com.jwatson.omnidig.Inventory.Items;
import com.jwatson.omnidig.Terrain.OmniTerrain;
import com.jwatson.omnidig.Terrain.TerrainChunk;
import com.jwatson.omnidig.World.ObjState;
import com.jwatson.omnidig.World.OmniWorld;
import com.jwatson.omnidig.World.WorldObj;
import com.jwatson.omnidig.events.DamageEvent;
import com.jwatson.omnidig.events.EventManager;
import com.jwatson.omnidig.events.InputEvent;
import com.jwatson.omnidig.events.TouchEvent;
import com.jwatson.omnidig.netplay.Server;
import com.jwatson.omnidig.ui.Console;
import com.jwatson.omnidig.ui.OnScreenText;



public class Player extends WorldObj {
	
	public static List<Player> spawnedPlayers;
	
	public static float SPEED = 20f;
	
	//jumping
	boolean shortJump;
	int autoJumpCounter,lastJumpCount;
	
	//Set for collisions
	List<TerrainChunk> chunksToTest;
	
	//Testing input strategies
	public List<input> stored_inputs;
	public List<input> pressed_inputs;
	
	//stats
	boolean left,right;
	
	//animation stuff

	Animation anim;
	float animTime;
	
	//For network
	public SocketAddress socket;
	public List<int[]> block_buffer;; 
	
	//Bag stuff
	public ItemObject[] bagItems;
	public int selected = -1;
	
	//Click pos
	float clickX;
	float clickY;
	
	//Listeners
	InputListener inputListener;
	TouchListener touchListener;
	
	//jump timer
	long lastJumpTick;

	public Player() {
		
		bounds.height = 0.8f;
		bounds.width = 0.6f;
		
		stun_time = 0.5f;
		
		stored_inputs = new ArrayList<input>();
		pressed_inputs = new ArrayList<input>();
		
		chunksToTest = new ArrayList<TerrainChunk>();
		block_buffer = new ArrayList<int[]>();
		
		this.walk_speed = 2;
		this.run_speed = 5;
		this.jump_strength = 6;
		
		this.MaxHP = 100;
		this.HP = MaxHP;
		
		this.friction = 0f;
		
		
		playAnim("bobIdleRight");
		
		//spawnedPlayers.add(this);
		
		bagItems = new ItemObject[Configuration.Bag_MaxItems];
		
		attackable = true;
		//createListeners();
	}
	

	void createListeners() {
		
		inputListener = new InputListener(this) {
			@Override
			public void onMove(InputEvent e) {
				Move(e);
			}
			
			
			
			@Override
			public void onTouch(TouchEvent e) {
				HandleClick(e);
			}
			
			
			@Override
			public void hit(DamageEvent e) {
				// TODO Auto-generated method stub
				onHit(e.dmged, e.item);
			}
			
			@Override
			public void onDamaged(DamageEvent e) {
				// TODO Auto-generated method stub
				onDamage(e.target, e.item);
				bounds.x = e.x;
				bounds.y = e.y;
			}
		
			
			
		};
		
		
		EventManager.addListener(inputListener);
		
	}
	
	public void Move(InputEvent e) {
		
		e.x = Float.valueOf(bounds.x);
		e.y = Float.valueOf(bounds.y);
		
		
		if(isDead) {
			if(stateTime >= Configuration.Player_SpawnTime) {
				respawn();
			}
			return;
		}
		
		
		if(e.direction < 2) {
			
			accel.x = e.direction * SPEED;
			
			if(e.direction != 0) {
				direction = e.direction;
				isMoving = true;
			}
			else
				isMoving = false;
			}
			else { //jump command
				Jump(e);
			}
	}
		
	@Override
	public void update(float delta) {

		while(block_buffer.size() > 3) {
			block_buffer.remove(0);
		}
		
		
		if(removalFlag) {
			spawnedPlayers.remove(this);
			OmniWorld.instance.spawnedObjects.remove(this);
		}
			
		
		//Player pl = OmniWorld.instance.client;
		
		//TerrainChunk ch = OmniTerrain.chunks[(int)pl.bounds.x/OmniTerrain.chunksize][(int)pl.bounds.y/OmniTerrain.chunksize];
		
//		ch.ambient[(int)pl.bounds.x%OmniTerrain.chunksize][(int)pl.bounds.y%OmniTerrain.chunksize] = (byte) Configuration.Lighting_MaxBrightness;
//		checkShortJump();
		checkAnim();
		updateItemAnim();
		CheckCollisions(bounds);
		CheckWaterCollision();
		
		if(stored_inputs.size() > 30) {
			stored_inputs.remove(0);
			stored_inputs.remove(1);
		}
		
		super.update(delta);
		
		if(isDead && stateTime >= 1f) {
			currentAnim = null;
		}
		
		if(!is_stunned)
		if(accel.x == 0) {			
			
			velX = 0;
			
		}
	}
	
	private void CheckWaterCollision() {
		
		int x1 = (int) (bounds.x / OmniTerrain.chunksize);
		int y1 = (int) (bounds.y / OmniTerrain.chunksize);
		float x2 = (bounds.x % OmniTerrain.chunksize);
		float y2 = (bounds.y % OmniTerrain.chunksize);
		
		TerrainChunk ch = OmniTerrain.chunks[x1][y1];
		
		int x3 = (int)(x2*2);
		int y3 = (int)(y2*2);
		
		if(ch.water_map[x3+y3*(OmniTerrain.chunksize*2)] > Configuration.Water_MaxBlocks/2f) {
			velX *= 0.75f;
			if(velY < 0)
			velY *= 0.75f;
		}
		
		
		
	}


	void checkShortJump() {
		if(shortJump) {
			
			
			velY *= 0.65f;
			shortJump = false;
		}
		
		if(lastJumpCount == autoJumpCounter)
			autoJumpCounter = 0;
		
		lastJumpCount = autoJumpCounter;
	}
	
	void updateItemAnim() {
		if(item_inuse != null) {
			switch(item_inuse.type.swing_type) {
			case Rotate:
				RotateItem(item_inuse);
				break;
			case Thrust:
				ThrustItem(item_inuse);
				break;
			default:
				break;
			}
		}
	}
	
	void checkAnim() {
		
		
		if(isDead)
			return;
		
		if(accel.x > 0)
			direction = 1;
		else if(accel.x < 0)
			direction = -1;
		
		if(grounded) {
			if(accel.x != 0) {
				
				if(accel.x > 0) {
					if(animName != "bobRight") {
					playAnim("bobRight");
					
					}
				}
				else if(accel.x < 0)
					playAnim("bobLeft");
				
				
			}

			if(velX == 0) {
				if(direction == -1)
					playAnim("bobIdleLeft");
				else
					playAnim("bobIdleRight");
			}
		}
		
		if(velY != 0) {
			if(direction == -1)
				playAnim("bobJumpLeft");
			else
				playAnim("bobJumpRight");
			grounded = false;
		}
	}
	
	public void Jump(InputEvent e) {
		if(grounded) {
		
		if(direction == 1)
			playAnim("bobJumpRight");
		else
			playAnim("bobJumpLeft");
			
		applyImpulse(0, 7);
		lastJumpTick = e.tick;
		grounded = false;
		}
		else {
			
			if(e.tick - lastJumpTick < Configuration.Movement_JumpCancleTime) {
				
					shortJump = true;

			}
		}
	}
	

	
	
	public void updateInput(float delta) {
		
		
		
	}
	
	void HandleClick(TouchEvent e) {
		
		if(isDead) {
			if(stateTime >= Configuration.Player_SpawnTime) {
				respawn();
			}
			return;
		}
		
		if(selected >= 0) {
			
			if(bagItems[selected] != null)
				if(bagItems[selected].stack > 0) {
					
					if(PlayItemAnim(bagItems[selected].item))
					bagItems[selected].item.type.OnUse(bagItems[selected], this, e.x, e.y);
					
					
				}
			
		}
		
	}
	
	public boolean canFire() {
		
			if(selected >= 0) {
			
			if(bagItems[selected] != null)
				if(bagItems[selected].stack > 0) {
					if(item_inuse == null || item_usetimer > item_inuse.type.swing_speed + item_inuse.type.reload_time) {
						return true;
					}
				}
			}
		
		return false;
	}

	void CheckCollisions(Rectangle bounds) {
		
		int p1x = (int)bounds.x / OmniTerrain.chunksize;
		int p1y = (int)bounds.y / OmniTerrain.chunksize;
		int p2x = (int)(bounds.x + bounds.width) / OmniTerrain.chunksize;
		int p2y = (int)bounds.y / OmniTerrain.chunksize;
		int p3x = (int)(bounds.x + bounds.width) / OmniTerrain.chunksize;
		int p3y = (int)(bounds.y + bounds.height) / OmniTerrain.chunksize;
		int p4x = (int)bounds.x / OmniTerrain.chunksize;
		int p4y = (int)(bounds.y + bounds.height) / OmniTerrain.chunksize;
		
		TerrainChunk ch1 = OmniTerrain.chunks[p1x][p1y];
		TerrainChunk ch2 = OmniTerrain.chunks[p2x][p2y];
		TerrainChunk ch3 = OmniTerrain.chunks[p3x][p3y];
		TerrainChunk ch4 = OmniTerrain.chunks[p4x][p4y];
		
		chunksToTest.add(ch1);
		
		if(!chunksToTest.contains(ch2))
		chunksToTest.add(ch2);
		
		if(!chunksToTest.contains(ch3))
		chunksToTest.add(ch3);
		
		if(!chunksToTest.contains(ch4))
		chunksToTest.add(ch4);
		
		if(item_inuse != null) {
			int i1x = (int)item_inuse.type.bounds.x / OmniTerrain.chunksize;
			int i1y = (int)item_inuse.type.bounds.y / OmniTerrain.chunksize;
			int i2x = (int)(item_inuse.type.bounds.x + item_inuse.type.bounds.width) / OmniTerrain.chunksize;
			int i2y = (int)item_inuse.type.bounds.y / OmniTerrain.chunksize;
			int i3x = (int)(item_inuse.type.bounds.x + item_inuse.type.bounds.width) / OmniTerrain.chunksize;
			int i3y = (int)(item_inuse.type.bounds.y + item_inuse.type.bounds.height) / OmniTerrain.chunksize;
			int i4x = (int)item_inuse.type.bounds.x / OmniTerrain.chunksize;
			int i4y = (int)(item_inuse.type.bounds.y + item_inuse.type.bounds.height) / OmniTerrain.chunksize;
			TerrainChunk ch1x = OmniTerrain.chunks[i1x][i1y];
			TerrainChunk ch2x = OmniTerrain.chunks[i2x][i2y];
			TerrainChunk ch3x = OmniTerrain.chunks[i3x][i3y];
			TerrainChunk ch4x = OmniTerrain.chunks[i4x][i4y];
			if(!chunksToTest.contains(ch1x))
			chunksToTest.add(ch1x);	
			if(!chunksToTest.contains(ch2x))
			chunksToTest.add(ch2x);			
			if(!chunksToTest.contains(ch3x))
			chunksToTest.add(ch3x);		
			if(!chunksToTest.contains(ch4x))
			chunksToTest.add(ch4x);
		}
		
		
		
		for(TerrainChunk ch : chunksToTest) {
			
			if(ch == null)
				continue;
			
			for(int i=0; i<ch.activeObjects.size(); i++) {
				
				
				WorldObj obj = ch.activeObjects.get(i);
				
				if(obj == this)
					continue;
				
				if(obj.bounds.overlaps(bounds)) {
					obj.onCollision(this);
					this.onCollision(obj);
				}
				if(!obj.is_stunned && !obj.isDead)
				if(item_inuse != null) {
					if(item_inuse.type.bounds != null)
					if(item_inuse.type.bounds.overlaps(obj.bounds)) {
						
						DamageEvent event = DamageEvent.allocate();
						event.set(OmniWorld.ticks, this, obj, item_inuse, 1, obj.bounds.x, obj.bounds.y);
						EventManager.addEvent(event);
						
					}
				}
				
			}
			
		}
		
		chunksToTest.clear();
		
	}

	void CheckAttackCollisions(Rectangle bounds) {
		
		int p1x = (int)bounds.x / OmniTerrain.chunksize;
		int p1y = (int)bounds.y / OmniTerrain.chunksize;
		int p2x = (int)(bounds.x + bounds.width) / OmniTerrain.chunksize;
		int p2y = (int)bounds.y / OmniTerrain.chunksize;
		int p3x = (int)(bounds.x + bounds.width) / OmniTerrain.chunksize;
		int p3y = (int)(bounds.y + bounds.height) / OmniTerrain.chunksize;
		int p4x = (int)bounds.x / OmniTerrain.chunksize;
		int p4y = (int)(bounds.y + bounds.height) / OmniTerrain.chunksize;

		
		TerrainChunk ch1 = OmniTerrain.chunks[p1x][p1y];
		TerrainChunk ch2 = OmniTerrain.chunks[p2x][p2y];
		TerrainChunk ch3 = OmniTerrain.chunks[p3x][p3y];
		TerrainChunk ch4 = OmniTerrain.chunks[p4x][p4y];
		
		chunksToTest.add(ch1);
		
		if(!chunksToTest.contains(ch2))
		chunksToTest.add(ch2);
		
		if(!chunksToTest.contains(ch3))
		chunksToTest.add(ch3);
		
		if(!chunksToTest.contains(ch4))
		chunksToTest.add(ch4);
		
		for(TerrainChunk ch : chunksToTest) {
			
			if(ch == null)
				continue;
			
			for(int i=0; i<ch.activeObjects.size(); i++) {
				
				
				WorldObj obj = ch.activeObjects.get(i);
				
				if(obj == this || !obj.attackable)
					continue;
				
				if(obj.bounds.overlaps(bounds)) {
					obj.onCollision(this);
					this.onCollision(obj);
				}
				
			}
			
		}
		
		chunksToTest.clear();
		
	}

	

	public void AddItemToBag(Items item, int amount) {
		
		int i = 0;
		

		
		while(amount > 0) {
			
			ItemObject obj = bagItems[i];
			
			if(obj == null) {
				if(amount > item.type.GetStackLimit())
					bagItems[i] = new ItemObject(item, item.type.GetStackLimit());
				else
					bagItems[i] = new ItemObject(item, amount
							);
				amount -= item.type.GetStackLimit();
				
			}
			
			else if(obj.item == item) {
				
				int stack_limit = obj.item.type.GetStackLimit();
				
				if(obj.stack < stack_limit) {
					
					if(obj.stack+amount > stack_limit) {						
						amount -= stack_limit - obj.stack;
						obj.stack = obj.item.type.GetStackLimit();
					}
					else {
						obj.stack += amount;
						amount = 0;
					}
					
				}
			}
			
			else if(obj.stack == 0) {
				if(amount > item.type.GetStackLimit())
					bagItems[i] = new ItemObject(item, item.type.GetStackLimit());
				else
					bagItems[i] = new ItemObject(item, amount);
				
				amount -= item.type.GetStackLimit();
			}
			
			
			i++;
			if(i > Configuration.Bag_MaxItems)
				i = 0;
			
		}
		
	}


	
	public void DropItem(int id, int amount) {
		
		ItemObject item = bagItems[id];
		
		
		
		item.bounds.setWidth(0.5f);
		item.bounds.setHeight(0.5f);
		
		
		
		if(item.stack <= amount) {
			amount = item.stack;
			bagItems[id] = null;
			OmniWorld.instance.spawnObject(item, bounds.x + 0.5f, bounds.y + 0.5f, 1, 1);
			item.velX = direction * 5;
			item.velY = 5;
			item.playAnim(item.item.toString());
		}
		else {
			bagItems[id].stack -= amount;
			ItemObject newobj = new ItemObject(item);
			newobj.stack = amount;
			newobj.playAnim(item.item.toString());
			newobj.velX = direction * 5;
			newobj.velY = 5;
			OmniWorld.instance.spawnObject(newobj, bounds.x + 0.5f, bounds.y + 0.5f, 1, 1);
		}
		
	}
	
	@Override
	public void onCollision(WorldObj obj) {
		// TODO Auto-generated method stub
		if(obj.removalFlag)
			return;
		
		
		if(obj.item != null) {
			
			
			if(obj.stateTime > 0.2f) {
				AddItemToBag(obj.item,obj.stack);
				obj.removalFlag = true;
				
				Gdx.app.debug("", ""+obj.stack);
			}
			
			
			
		}
		
		
		
		super.onCollision(obj);
	}

	public void TryMoveHeld(int from, int to,int amt) {
		
		
		ItemObject item = bagItems[to];
		
		
		if(item == null) {
			
			if(bagItems[from].stack - amt > 0) {
				bagItems[from].stack -= amt;
				bagItems[to] = new ItemObject(bagItems[from].item, amt);
			}
			else {
				bagItems[to] = bagItems[from];
				bagItems[from] = null;
				
			}
			return;
		}
		
		if(item.item == bagItems[from].item) {
			
			if(item.stack + amt > item.item.type.GetStackLimit()) {
				
				
				
				bagItems[from].stack -= item.item.type.GetStackLimit() - item.stack;
				
				item.stack = item.item.type.GetStackLimit();
				return;
			}
			else {
				item.stack += amt;
				bagItems[from] = null;
				return;
			}
			
		}
		else {
			
			if(bagItems[from].stack - amt <= 0) {
				
				bagItems[to] = bagItems[from];
				bagItems[from] = item;
				return;
				
			}
			
		}
		
		
	}
	public int counter,counter2;
	@Override
	public void onHit(WorldObj obj, Items item) {
		// TODO Auto-generated method stub
		
		counter++;
	}
	
	@Override
	public void onDamage(WorldObj obj, Items item) {
		// TODO Auto-generated method stub

		if(is_stunned) {
			return;
		}
		
		int dmg = (int) item.type.ATK;
		
		OnScreenText.AddText(""+dmg, bounds.x , bounds.y + 1.1f);
		
		velX = obj.direction * 5;
		velY = 5;
		grounded = false;
		
		HP -= dmg;
		

		stun_counter = 0;
		
		if(HP <= 0 && !isDead) {
			onDeath();
		}
		
	}
	
	public void onDeath() {
		
		
		HP = 0;
		isDead = true;
		playAnim("bobDying");
		stateTime = 0;
	}
	
	@Override
	public void respawn() {
		// TODO Auto-generated method stub
		bounds.x = OmniWorld.SpawnX;
		bounds.y = OmniWorld.SpawnY;
		HP = MaxHP;
		isDead = false;
		stateTime = 0;
	}
	
	@Override
	protected void collidedWithTerrain(Rectangle rect, boolean x, boolean y) {
		super.collidedWithTerrain(rect, x, y);
		
		if(x) {
			Items item = OmniTerrain.getBlock((int)rect.x, (int)(rect.y)+1);
			if(item == Items.Empty || item == Items.MAT_Grass) {
				if(grounded) {
					autoJumpCounter++;
					
					if(autoJumpCounter >= Configuration.Movement_AutoJumpDelay) {
					grounded = false;
					velY = 5 * Configuration.TICK_RATE;
					autoJumpCounter = 0;
					}
				}
			}
		}
	}
	
}



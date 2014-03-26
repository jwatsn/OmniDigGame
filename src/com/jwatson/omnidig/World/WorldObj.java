package com.jwatson.omnidig.World;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.jwatson.omnidig.Configuration;
import com.jwatson.omnidig.Assets.AssetManager;
import com.jwatson.omnidig.Gfx.CustomBatch;
import com.jwatson.omnidig.Inventory.ItemObject;
import com.jwatson.omnidig.Inventory.ItemType.Swing_Type;
import com.jwatson.omnidig.Inventory.Items;
import com.jwatson.omnidig.Lighting.LightingManager;
import com.jwatson.omnidig.Managers.InputManager;
import com.jwatson.omnidig.Managers.Manager;
import com.jwatson.omnidig.Player.CommandHandler;
import com.jwatson.omnidig.Player.Player;
import com.jwatson.omnidig.Screens.GameScreen;
import com.jwatson.omnidig.Terrain.OmniTerrain;
import com.jwatson.omnidig.Terrain.TerrainChunk;
import com.jwatson.omnidig.netplay.Server;
import com.jwatson.omnidig.ui.Console;



public class WorldObj extends Actor {
	
	protected class frame {
		
		
		
		
		
		Vector2 offset;
		TextureRegion tex;
		float width,height;
		
		
		
		public frame(TextureRegion tex, float offset_x, float offset_y,float width,float height) {
			offset = new Vector2(offset_x, offset_y);
			this.tex = tex;
			
			this.width = width;
			this.height = height;
			
			
		}
	}
	
	public Items item;
	public int stack;
	
	public boolean needsUpdate;
	public boolean isMoving;
	protected boolean netObj;
	
	
	public InputManager inputManager;
	//Manager list
	public List<Manager> activeManagers;

	//timers
	public float stateTime;
	public static int numObjs;
	
	//Animation info
	public String animName = "";
	String lastAnimName = "";
	protected Animation currentAnim;
	
	
	//Current Terrain Chunk
	public TerrainChunk chunk;
	
	//stats
	public boolean isDead;
	public int HP;
	public int MaxHP;
	public int DEF;
	
	//Object vars
	public int id;
	
	//Attackable
	public boolean attackable;
	
	//Commands
	protected Map<String, Integer> Commands;
	
	
	//Item animation IE Swinging a pickaxe
	public Items item_inuse;
	public Items item_next;
	public float item_usetimer;
	public float item_rotation;
	
	
	//removal flag
	public boolean removalFlag;
	
	//Physic variables	
	public boolean usesPhysics = true;
	public static float GRAVITY = 9.8f;
	public static int LEFT = -1;
	public static int RIGHT = 1;	
	public float width = 1;
	public float height = 1;
	protected float friction;
	protected float walk_speed;
	protected float run_speed = 999;
	protected float jump_strength;
	
	protected float stun_time;
	protected float stun_counter;
	public boolean is_stunned;
	
	public boolean grounded;
	
	public double velX;
	public double velY;
	
	public int direction = 1;
	
	//for rendering interpolation
	float tick_length;
	long last_tick = -1;
	
	public Vector2 pos,accel,vel,lastpos,lastbounds;
	public Rectangle bounds;
	
	int lastHash;
	
	public WorldObj() {
		
		//set up manager list
		activeManagers = new ArrayList<Manager>();
		
		
		//Set up texture frames
		//frames = new ArrayList<WorldObj.frame>();
		
		//Set up physics vars
		pos = new Vector2();
		lastpos = new Vector2();
		lastbounds = new Vector2();
		vel = new Vector2();
		accel = new Vector2();
		bounds = new Rectangle();
		inputManager = new InputManager(this);

		id = numObjs;
		numObjs++;
		
		width = 1;
		height = 1;
		
		
	}
	
	public void super_update(float delta) { //for simulating more than 1 tick at a time ie. correcting prediction
		Move(delta);
	}
	
	public void update(float delta) {
		
		//update active managers
		inputManager.update(delta);
		
		
		for(Manager manager : activeManagers)
			manager.update(delta);
		
		lastpos.set(bounds.x, bounds.y);
		
		
		
		if(usesPhysics)
		Move(delta);
		
		
		
		
		
		
		if(lastpos.x != bounds.x || lastpos.y != bounds.y || !lastAnimName.equals(animName) || item_inuse != null) {
			needsUpdate = true;
			
			if((int)lastpos.x != (int)bounds.x || (int)lastpos.y != (int)bounds.y )
				LightingManager.instance.updateAll();
		}
		
		if(item_inuse != null) {
			
			
			if(animName.charAt(animName.length()-1) != '#') {
				animName += "#";
			}
			
			
			
		}
		
		
		
		
		if(!lastAnimName.equals(animName)) {			
			currentAnim = AssetManager.getAnim(animName);
		}
		
		lastAnimName = animName;
		
		
		if(stun_counter < stun_time)
			is_stunned = true;
		else {
			if(grounded)
				is_stunned = false;
		}
		
		
		UpdateTimers(delta);
	}
	
	
	
	Vector2 bPos = new Vector2();
	Vector2 bPos2 = new Vector2();
	
	public void render(CustomBatch batch, float delta) {	
	


		InterpolatePos();
		
		if(item_inuse != null)
			RenderItemAnim(batch);
		
		DrawAnim(batch);	
		
		
	}
	
	
	
	void Move(float delta) {
		
		accel.y = -GRAVITY;
		
		
										
		velY += accel.y * delta;
		
		if(!is_stunned) {
		velX += accel.x * delta;
		if(velX > run_speed)
			velX = run_speed;
		if(velX < -run_speed)
			velX = -run_speed;
		}
		
		velX *= delta;
		velY *= delta;
		
		bounds.x += velX;
		
		collisionTest();
		
		
		boolean flag = false; // Auto jump flag -- kinda messy
		for(int i=0; i<r.length; i++) {
			Rectangle rect = r[i];
			if(bounds.overlaps(rect)) {
				
				collidedWithTerrain(rect,true,false);
				
			}
			
			
		}
		
		bounds.y += velY;
		
		collisionTest();
		
		for(int i=0; i<r.length; i++) {
			Rectangle rect = r[i];
			if(bounds.overlaps(rect)) {
				
			collidedWithTerrain(rect, false, true);

			}
		}

		
		velX /= delta;
		velY /= delta;
		
		vel.set((float)velX,(float)velY);
		
		
//		pos.x = bounds.x;
//		pos.y = bounds.y;
		
		
		
		
		
	}
	
	protected void collidedWithTerrain(Rectangle rect,boolean x, boolean y) {
		
		if(x) {
		
		if (velX < 0)
			bounds.x = rect.x + rect.width + 0.01f;
		else
			bounds.x = rect.x - bounds.width - 0.01f;
		
		
		velX = 0;
		}
		else if(y) {
			if(!grounded) {
				if(velY <= 0)
					grounded = true;	
			}
			if(velY > 0) {
				bounds.y = rect.y - bounds.height - 0.01f;
			}
			else {
				bounds.y = rect.y + rect.height + 0.01f;
			}
			velY = 0;
		}
		
	}
	
	Rectangle[] r = { new Rectangle(), new Rectangle(), new Rectangle(),new Rectangle() };
	
	void DrawAnim(CustomBatch batch) {
		
		
		
		if(currentAnim != null) {
			batch.draw(currentAnim.getKeyFrame(stateTime),1,pos.x, pos.y,width,height);
		}
	}
	
	
	
	
	
	void collisionTest() {
		
		
		
		int p1x = (int)bounds.x;
		int p1y = (int)bounds.y;
		int p2x = (int)(bounds.x + bounds.width);
		int p2y = (int)bounds.y;
		int p3x = (int)(bounds.x + bounds.width);
		int p3y = (int)(bounds.y + bounds.height);
		int p4x = (int)bounds.x;
		int p4y = (int)(bounds.y + bounds.height);
		
		
		
		if(OmniTerrain.isSolid(item,p1x, p1y)) {
			Items item = OmniTerrain.chunks[p1x/OmniTerrain.chunksize][p1y/OmniTerrain.chunksize].map[p1x%OmniTerrain.chunksize][p1y%OmniTerrain.chunksize];
			r[0].set(p1x, p1y, item.type.bounds.width, item.type.bounds.height);
		}
		else
			r[0].set(-1,-1,-1,-1);
		
		if(OmniTerrain.isSolid(p2x, p2y)) {
			Items item = OmniTerrain.chunks[p2x/OmniTerrain.chunksize][p2y/OmniTerrain.chunksize].map[p2x%OmniTerrain.chunksize][p2y%OmniTerrain.chunksize];
			r[1].set(p2x, p2y, item.type.bounds.width, item.type.bounds.height);
		}
		else
			r[1].set(-1,-1,-1,-1);
		
		if(OmniTerrain.isSolid(p3x, p3y)) {
			Items item = OmniTerrain.chunks[p3x/OmniTerrain.chunksize][p3y/OmniTerrain.chunksize].map[p3x%OmniTerrain.chunksize][p3y%OmniTerrain.chunksize];
			r[2].set(p3x, p3y, item.type.bounds.width, item.type.bounds.height);
		}
		else
			r[2].set(-1,-1,-1,-1);
		
		if(OmniTerrain.isSolid(p4x, p4y)) {
			Items item = OmniTerrain.chunks[p4x/OmniTerrain.chunksize][p4y/OmniTerrain.chunksize].map[p4x%OmniTerrain.chunksize][p4y%OmniTerrain.chunksize];
			r[3].set(p4x, p4y,item.type.bounds.width, item.type.bounds.height);
		}
		else
			r[3].set(-1,-1,-1,-1);
		
		
	}
	
	void InterpolatePos() {	
		float diff = GameScreen.worldUpdateTimer / Configuration.TICK_RATE;
		bPos2.set(lastpos.x -0.2f, lastpos.y);
		bPos.set(bounds.x - 0.2f, bounds.y);	
		bPos2.lerp(bPos, diff);
		pos.set(bPos2);		
	}
	
	void checkChunk() {
		
		
		if((int)pos.x / chunk.size != chunk.x2 || (int)pos.y / chunk.size != chunk.y2) {
			chunk = OmniTerrain.chunks[(int)bounds.x / chunk.size][(int)bounds.y / chunk.size];
		}
	}
	
	public void applyImpulse(float x, float y) {
		
		velX += x;
		velY += y;
		
	}
	
	public void setImpulseX(float x) {
		velX = x;
	}
	
	public void playAnim(String name) {
		
		try {
			
			
			
			
			
			animName = name;

			
		}
		catch(Exception e) {
			
		}
		
	}
	
	public float getWidth() { return width; }
	public float getHeight() { return height; }
	
	public void onCollision(WorldObj obj) {
		
	}
	
	void UpdateTimers(float delta) {
		
		
		
		if(item_inuse != null)
		item_usetimer += delta;
		
		
		stun_counter += delta;
		stateTime += delta;
	}
	
	void RenderItemAnim(CustomBatch batch) {
		
		
		if(item_inuse.type.swing_type == Swing_Type.Rotate) {
	
			float x = (pos.x - item_inuse.type.offset.x) + 0.5f + (0.4375f*direction);
			float y = (pos.y - item_inuse.type.offset.x) + 0.5f;
			
			batch.draw(item_inuse.texture,x,y,item_inuse.type.offset.x,item_inuse.type.offset.y,1,1,(-direction)*item_inuse.type.scale,item_inuse.type.scale,item_rotation);
			
		}
		else {
			float x = (pos.x - item_inuse.type.offset.x) + 0.5f + (0.2375f*direction);
			float y = (pos.y - item_inuse.type.offset.x) + 0.4375f;
			
			x += item_rotation;
			
			batch.draw(item_inuse.texture,x,y,item_inuse.type.offset.x,item_inuse.type.offset.y,1,1,item_inuse.type.scale*direction,direction*item_inuse.type.scale,item_inuse.type.rotation_offset);
		}
	}
	
	public boolean PlayItemAnim(Items item) {
		
		if(item_inuse == null) {
			item_inuse = item;
			item_usetimer = 0;
			return true;
		}
		
		if(item_usetimer > item_inuse.type.swing_speed + item_inuse.type.reload_time) {
			item_inuse = item;
			item_usetimer = 0;
			return true;
		}
		
		return false;
		
		
	
	}
	
	protected boolean RotateItem(Items item) {
		
		if(item_usetimer > item_inuse.type.swing_speed + item_inuse.type.reload_time + item_inuse.type.deadtime) {
			item_usetimer = 0;		
			item_inuse = null;
			return false;
			
		}
		
		
		
		float r_offset = item.type.rotation_offset * -direction;
		
		float a = (item_usetimer / item.type.swing_speed);
		
		if(a>1)
			a=1;
		
		item_rotation = r_offset - (item.type.rotation_max * a) * direction;
		
		
		
		return true;
		
	}
	
	protected void ThrustItem(Items item) {
		
		if(item_usetimer > item_inuse.type.swing_speed + item_inuse.type.reload_time + item_inuse.type.deadtime) {
			item_usetimer = 0;		
			item_inuse = null;
			return;
			
		}
		
		float a =  (item_usetimer / item.type.swing_speed);
		
		if(a>1)
			a=1;
		item.type.bounds.width = a * item.type.width;

		a *= (180 * direction);
		
		item.type.bounds.x = bounds.x +0.5f + (0.45f * direction);
		if(direction < 0)
			item.type.bounds.x -= item.type.bounds.width;
		item.type.bounds.y = bounds.y + item.type.height/4f;
		
		item_rotation = (float)Math.sin(Math.toRadians(a)) * item.type.rotation_max;
	}
	
	public void onHit(WorldObj obj, Items item) {
		
	}
	public void onDamage(WorldObj obj, Items item) {
		
	}

	public boolean isGrounded() {
		// TODO Auto-generated method stub
		return grounded;
	}

	public Manager getManager(Class type) {
		
		for(Manager man : activeManagers) {
			if(man.getClass() == type) {
				return man;
			}
		}

		return null;
	}

	public void respawn() {
		// TODO Auto-generated method stub
		
	}

	public void spawn() {
		// TODO Auto-generated method stub
		
	}

}

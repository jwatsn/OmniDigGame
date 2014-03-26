package com.jwatson.omnidig.Inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.jwatson.omnidig.World.WorldObj;

public abstract class ItemType {
	
	public enum Swing_Type {
		Thrust,
		Rotate;
	}
	
	//damage info
	public int ATK;
	
	//anim for alpha shading
	public TextureRegion[] damaged_alpha;
	
	//timing stuff
	public float deadtime = 0.1f; // how long the animation lingers after its done (preventing the arm from dropping while holding down fire)
	
	//For swinging (if its a weapon)
	public Swing_Type swing_type;
	public float rotation_offset;
	public float rotation_max;
	public float reload_time;
	public float scale = 1;
	
	//offset
	public Vector2 offset;
	
	//misc
	protected boolean solid,placeable,is_step;
	protected int stack_limit = 1;
	public float swing_speed;
	public int maxhp;
	
	//bounding info
	public Rectangle bounds;
	public float width;
	public float height;
	

	
//	---------------------------------------------------------------------------------------------------
	public ItemType(float swing_speed, int ATK) {
		this.swing_speed = swing_speed;
		this.ATK = ATK;
	}
	
	public ItemType(float swing_speed) {
		this.swing_speed = swing_speed;
	}
	
	public ItemType() {
		
	}
//	---------------------------------------------------------------------------------------------------

	
	
	public String desc;
	
	public boolean isSolid() { return solid; }
	
	public abstract void OnUse(ItemObject item,WorldObj obj,float x, float y);
	
	public int GetStackLimit() {
		return stack_limit;
	}
	
	
//	Vector2 pos1 = new Vector2();
//	Vector2 pos2 = new Vector2();
	protected float getAngle(WorldObj obj,float x, float y) {
		
		float x2 = ((int)x+0.5f) - ((int)obj.bounds.x + 0.5f);
		float y2 = ((int)y+0.5f) - ((int)obj.bounds.y + 0.5f);
		
		float angle = (float) MathUtils.atan2(y2, x2);
		
		
		
		return angle;
	}

}
